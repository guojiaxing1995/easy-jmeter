package io.github.guojiaxing1995.easyJmeter.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.corundumstudio.socketio.SocketIOServer;
import com.github.benmanes.caffeine.cache.Cache;
import io.github.guojiaxing1995.easyJmeter.common.LocalUser;
import io.github.guojiaxing1995.easyJmeter.common.enumeration.JmeterStatusEnum;
import io.github.guojiaxing1995.easyJmeter.common.jmeter.JmeterExternal;
import io.github.guojiaxing1995.easyJmeter.dto.jcase.CaseDebugDTO;
import io.github.guojiaxing1995.easyJmeter.dto.jcase.CreateOrUpdateCaseDTO;
import io.github.guojiaxing1995.easyJmeter.mapper.CaseMapper;
import io.github.guojiaxing1995.easyJmeter.mapper.JFileMapper;
import io.github.guojiaxing1995.easyJmeter.model.CaseDO;
import io.github.guojiaxing1995.easyJmeter.model.JFileDO;
import io.github.guojiaxing1995.easyJmeter.service.CaseService;
import io.github.guojiaxing1995.easyJmeter.service.JFileService;
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

import java.io.File;
import java.io.IOException;
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
    public JSONObject debugCase(CaseDebugDTO caseDebugDTO) throws IOException {
        Integer caseId = caseDebugDTO.getCaseId();
        Long debugId = caseDebugDTO.getDebugId();
        CaseDO caseDO = caseMapper.selectById(caseId);
        if (caseDO == null){
            throw new NotFoundException(12201);
        }
        // 初始化服务端配置
        JmeterExternal jmeterExternal = new JmeterExternal();
        jmeterExternal.initServerDebugJmeterUtils();
        // jtl文件路径
        String jtlPath = null;
        // 如果debug没有配置过，则进行配置
        if (caffeineCache.getIfPresent(caseId + "_config_" + debugId) == null) {
            // 配置jmx文件，缓存jmx文件路径
            Map<String, Object> map = jmeterExternal.editJmxDebugConfig(caseDO, debugId, jFileService);
            jtlPath = map.get("jtlPath").toString();
            log.info(map.toString());
            caffeineCache.put(caseId + "_config_" + debugId, map.get("jmxPath"));
            // 发送配置完成消息
            JSONObject result = new JSONObject();
            result.put("config", true);
            result.put("caseDebugDTO", caseDebugDTO);
            socketServer.getRoomOperations("web").sendEvent("caseDebugResult", result);
        }
        // 获取jmx路径
        String jmxPath = (String) caffeineCache.getIfPresent(caseId + "_config_" + debugId);
        // 进行调试 发起请求
        HashTree testPlanTree = SaveService.loadTree(new File(jmxPath));
        StandardJMeterEngine engine = new StandardJMeterEngine();
        JMeter.convertSubTree(testPlanTree,false);
        engine.configure(testPlanTree);
        engine.run();
        engine.exit();

        // jtl文件处理


        return null;
    }
}
