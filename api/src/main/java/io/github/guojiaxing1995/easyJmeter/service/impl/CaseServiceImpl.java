package io.github.guojiaxing1995.easyJmeter.service.impl;

import com.corundumstudio.socketio.SocketIOServer;
import com.github.benmanes.caffeine.cache.Cache;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import io.github.guojiaxing1995.easyJmeter.common.LocalUser;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.DebugTypeEnum;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.JmeterExternal;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.xstream.AssertionResult;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.xstream.HttpSample;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.xstream.JmeterLogDeal;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.xstream.TestResults;
import io.github.guojiaxing1995.easyJmeter.dto.jcase.CaseDebugDTO;
import io.github.guojiaxing1995.easyJmeter.dto.jcase.CreateOrUpdateCaseDTO;
import io.github.guojiaxing1995.easyJmeter.mapper.CaseMapper;
import io.github.guojiaxing1995.easyJmeter.mapper.JFileMapper;
import io.github.guojiaxing1995.easyJmeter.model.CaseDO;
import io.github.guojiaxing1995.easyJmeter.model.JFileDO;
import io.github.guojiaxing1995.easyJmeter.service.CaseService;
import io.github.guojiaxing1995.easyJmeter.service.JFileService;
import io.github.guojiaxing1995.easyJmeter.vo.CaseDebugVO;
import io.github.guojiaxing1995.easyJmeter.vo.CaseInfoPlusVO;
import io.github.guojiaxing1995.easyJmeter.vo.CaseInfoVO;
import io.github.guojiaxing1995.easyJmeter.vo.JFileVO;
import io.github.talelin.autoconfigure.exception.NotFoundException;
import io.github.talelin.autoconfigure.exception.ParameterException;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.save.SaveService;
import org.apache.jorphan.collections.HashTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CaseServiceImpl implements CaseService {

    @Autowired
    private CaseMapper caseMapper;

    @Autowired
    private JFileMapper fileMapper;

    @Autowired
    Cache<String, Object> caffeineCache;

    @Autowired
    private JFileService jFileService;

    @Autowired
    private SocketIOServer socketServer;

    @Override
    public boolean createCase(CreateOrUpdateCaseDTO caseDTO) {
        CaseDO caseDO = new CaseDO();
        caseDO.setName(caseDTO.getName());
        caseDO.setCreator(LocalUser.getLocalUser().getId());
        caseDO.setDescription(caseDTO.getDescription());
        caseDO.setJmx(caseDTO.getJmx());
        caseDO.setCsv(caseDTO.getCsv());
        caseDO.setJar(caseDTO.getJar());
        caseDO.setProject(caseDTO.getProject());
        caseDO.setStatus(JmeterStatusEnum.IDLE);
        return caseMapper.insert(caseDO) > 0;
    }

    @Override
    public boolean updateCase(CaseDO caseDO, CreateOrUpdateCaseDTO caseDTO) {
        if (caseDO == null){
            throw new NotFoundException(12201);
        }
        if (caseDO.getStatus() != JmeterStatusEnum.IDLE) {
            throw new ParameterException(12304);
        }
        caseDO.setName(caseDTO.getName());
        caseDO.setDescription(caseDTO.getDescription());
        caseDO.setJmx(caseDTO.getJmx());
        caseDO.setCsv(caseDTO.getCsv());
        caseDO.setJar(caseDTO.getJar());
        caseDO.setProject(caseDTO.getProject());
        return caseMapper.updateById(caseDO) > 0;
    }

    public CaseDO getById(Integer id) {
        return caseMapper.selectById(id);
    }

    @Override
    public boolean deleteCase(Integer id) {
        CaseDO caseDO = caseMapper.selectById(id);
        if (caseDO == null){
            throw new NotFoundException(12201);
        }
        return caseMapper.deleteById(id) > 0;
    }

    @Override
    public List<CaseInfoVO> selectCase(Integer id) {
        List<CaseInfoVO> caseInfoVOS = caseMapper.select(id);
        for (CaseInfoVO caseInfoVO : caseInfoVOS) {
            if (caseInfoVO.getStatus() == JmeterStatusEnum.RUN) {
                caseInfoVO.setTaskProgress((HashMap<String, Object>) caffeineCache.getIfPresent(caseInfoVO.getTaskId() + "_PROGRESS"));
            }
        }
        return caseInfoVOS;
    }

    @Override
    public CaseInfoPlusVO getCaseInfoById(Integer id) {
        CaseDO caseDO = caseMapper.selectById(id);
        if (caseDO == null){
            throw new NotFoundException(12201);
        }
        List<JFileVO> jmxFileList = new ArrayList<JFileVO>();
        List<JFileVO> csvFileList = new ArrayList<JFileVO>();
        List<JFileVO> jarFileList = new ArrayList<JFileVO>();

        String jmxStr = caseDO.getJmx();
        List<JFileDO> jmxFileDOList= Arrays.stream(jmxStr.isEmpty() ? new String[]{} : jmxStr.split(","))
                .map(Integer::parseInt).map(jId -> fileMapper.selectById(jId)).collect(Collectors.toList());
        for (JFileDO fileDO : jmxFileDOList) {
            JFileVO fileVO;
            if (fileDO.getSize()/(1024 * 1024) >= 1) {
                fileVO = new JFileVO(fileDO, String.format("%.2f", fileDO.getSize()/(float)(1024 * 1024)) + "MB");
            } else if (fileDO.getSize()/1024 >= 1) {
                fileVO= new JFileVO(fileDO, String.format("%.2f", fileDO.getSize()/(float)1024) + "KB");
            } else {
                fileVO = new JFileVO(fileDO, fileDO.getSize() + "B");
            }
            jmxFileList.add(fileVO);
        }

        String csvStr = caseDO.getCsv();
        List<JFileDO> csvFileDOList= Arrays.stream(csvStr.isEmpty() ? new String[]{} : csvStr.split(","))
                .map(Integer::parseInt).map(jId -> fileMapper.selectById(jId)).collect(Collectors.toList());
        for (JFileDO fileDO : csvFileDOList) {
            JFileVO fileVO;
            if (fileDO.getSize()/(1024 * 1024) >= 1) {
                fileVO = new JFileVO(fileDO, String.format("%.2f", fileDO.getSize()/(float)(1024 * 1024)) + "MB");
            } else if (fileDO.getSize()/1024 >= 1) {
                fileVO= new JFileVO(fileDO, String.format("%.2f", fileDO.getSize()/(float)1024) + "KB");
            } else {
                fileVO = new JFileVO(fileDO, fileDO.getSize() + "B");
            }
            csvFileList.add(fileVO);
        }

        String jarStr = caseDO.getJar();
        List<JFileDO> jarFileDOList= Arrays.stream(jarStr.isEmpty() ? new String[]{} : jarStr.split(","))
                .map(Integer::parseInt).map(jId -> fileMapper.selectById(jId)).collect(Collectors.toList());
        for (JFileDO fileDO : jarFileDOList) {
            JFileVO fileVO;
            if (fileDO.getSize()/(1024 * 1024) >= 1) {
                fileVO = new JFileVO(fileDO, String.format("%.2f", fileDO.getSize()/(float)(1024 * 1024)) + "MB");
            } else if (fileDO.getSize()/1024 >= 1) {
                fileVO= new JFileVO(fileDO, String.format("%.2f", fileDO.getSize()/(float)1024) + "KB");
            } else {
                fileVO = new JFileVO(fileDO, fileDO.getSize() + "B");
            }
            jarFileList.add(fileVO);
        }

        return new CaseInfoPlusVO(caseDO, jmxFileList,csvFileList, jarFileList);
    }

    @Override
    public boolean updateCaseStatus(CaseDO caseDO, JmeterStatusEnum status) {
        caseDO.setStatus(status);
        return caseMapper.updateById(caseDO) > 0;
    }

    @Override
    public void debugCase(CaseDebugDTO caseDebugDTO) throws IOException {
        Integer caseId = caseDebugDTO.getCaseId();
        Long debugId = caseDebugDTO.getDebugId();
        CaseDO caseDO = caseMapper.selectById(caseId);
        String configKey = caseId + "_config_" + debugId;
        if (caseDO == null){
            throw new NotFoundException(12201);
        }
        // 初始化服务端配置
        JmeterExternal jmeterExternal = new JmeterExternal();
        jmeterExternal.initServerJmeterUtils(null);
        // 如果debug没有配置过，则进行配置
        if (caffeineCache.getIfPresent(configKey) == null) {
            // 配置jmx文件，缓存jmx文件路径
            Map<String, String> map = jmeterExternal.editJmxDebugConfig(caseDO, debugId, jFileService);
            caffeineCache.put(configKey, map);
            CaseDebugVO caseDebugVO = CaseDebugVO.builder().type(DebugTypeEnum.CONFIG).caseId(caseId).debugId(debugId).build();
            // 通知web端配置完成
            socketServer.getRoomOperations("web").sendEvent("caseDebugResult", caseDebugVO);
        }
        Map<String, String> map = (Map<String, String>) caffeineCache.getIfPresent(configKey);
        log.info(map.toString());
        // 获取jmx jtl路径
        String jmxPath = map.get("jmxPath");
        String jtlPath = map.get("jtlPath");
        // 进行调试 发起请求
        long l = System.currentTimeMillis();
        synchronized(this) {
            log.info(configKey+l+":debug_start");
            HashTree testPlanTree = SaveService.loadTree(new File(jmxPath));
            StandardJMeterEngine engine = new StandardJMeterEngine();
            JMeter.convertSubTree(testPlanTree,false);
            engine.configure(testPlanTree);
            engine.run();
            engine.exit();
            log.info(configKey+l+":debug_end");
        }

        // jtl文件处理
        try (BufferedReader reader = new BufferedReader(new FileReader(jtlPath))) {
            // 读取 XML 文件内容
            StringBuilder xmlContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                xmlContent.append(line).append("\n");
            }

            XStream xStream = new XStream();
            xStream.addPermission(AnyTypePermission.ANY);
            xStream.processAnnotations(HttpSample.class);
            xStream.processAnnotations(TestResults.class);
            xStream.processAnnotations(AssertionResult.class);
            TestResults testResults = (TestResults) xStream.fromXML(xmlContent.toString());

            CaseDebugVO caseDebugVO = CaseDebugVO.builder().type(DebugTypeEnum.SAMPLE).caseId(caseId).debugId(debugId).result(testResults).build();
            // 通知web端数据请求完成
            socketServer.getRoomOperations("web").sendEvent("caseDebugResult", caseDebugVO);
        }
        // 获取jmeter日志
        String logsPath = Paths.get(System.getProperty("user.dir"), "logs", "jmeter.log").toString();
        String[] logs = JmeterLogDeal.cutContentFromFile(logsPath, configKey + l + ":debug_start", configKey + l + ":debug_end");
        CaseDebugVO caseDebugVO = CaseDebugVO.builder().type(DebugTypeEnum.LOG).caseId(caseId).debugId(debugId).build();
        for (String log:logs) {
            caseDebugVO.setLog(log);
            socketServer.getRoomOperations("web").sendEvent("caseDebugResult", caseDebugVO);
        }
        // 清理jtl文件
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(jtlPath), StandardOpenOption.TRUNCATE_EXISTING)) {
            log.info("清空jtl文件");
        }

    }
}
