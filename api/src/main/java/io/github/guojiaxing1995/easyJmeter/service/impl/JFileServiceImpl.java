package io.github.guojiaxing1995.easyJmeter.service.impl;

import io.github.guojiaxing1995.easyJmeter.mapper.JFileMapper;
import io.github.guojiaxing1995.easyJmeter.model.JFileDO;
import io.github.guojiaxing1995.easyJmeter.module.file.FileProperties;
import io.github.guojiaxing1995.easyJmeter.service.JFileService;
import io.github.guojiaxing1995.easyJmeter.vo.CutFileVO;
import io.github.guojiaxing1995.easyJmeter.vo.JFileVO;
import io.github.talelin.autoconfigure.exception.FailedException;
import io.github.talelin.autoconfigure.exception.NotFoundException;
import io.github.talelin.autoconfigure.exception.ParameterException;
import io.minio.DownloadObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class JFileServiceImpl implements JFileService {

    private final MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Autowired
    public JFileServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Autowired
    private FileProperties fileProperties;

    @Autowired
    private JFileMapper jFileMapper;

    @Override
    public JFileVO createFile(MultipartFile file) {
        String name = file.getOriginalFilename();
        String fileExtension = StringUtils.getFilenameExtension(name);
        if (!Arrays.asList("csv", "jar", "jmx").contains(fileExtension)) {
            throw new ParameterException(12102);
        }
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String objectName = timestamp + "." + fileExtension;
        try {
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(file.getInputStream(), file.getSize(), -1).build());
        } catch (Exception e) {
            log.info(e.toString());
            throw new FailedException(12101);
        }
        JFileDO jFileDO = new JFileDO();
        jFileDO.setName(name);
        jFileDO.setPath("/" + bucketName + "/" + objectName);
        jFileDO.setUrl(endpoint + "/" + bucketName + "/" + objectName);
        jFileDO.setSize(file.getSize());
        jFileDO.setType(file.getContentType());
        jFileMapper.insert(jFileDO);

        JFileDO fileDO = jFileMapper.selectById(jFileDO.getId());
        if (fileDO.getSize()/(1024 * 1024) >= 1) {
            return new JFileVO(fileDO, String.format("%.2f", fileDO.getSize()/(float)(1024 * 1024)) + "MB");
        } else if (fileDO.getSize()/1024 >= 1) {
            return new JFileVO(fileDO, String.format("%.2f", fileDO.getSize()/(float)1024) + "KB");
        } else {
            return new JFileVO(fileDO, fileDO.getSize() + "B");
        }
    }

    @Override
    public Boolean setFileCut(Integer id, Boolean cut) {
        JFileDO jFileDO = jFileMapper.selectById(id);
        if (jFileDO == null){
            throw new NotFoundException(12103);
        }
        jFileDO.setCut(cut);
        return jFileMapper.updateById(jFileDO) > 0;
    }

    @Override
    public String downloadFile(Integer id, String dir){
        JFileDO jFileDO = jFileMapper.selectById(id);
        String filePath;
        if (dir == null) {
            String format = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            Path path = Paths.get(fileProperties.getStoreDir(), format).toAbsolutePath();
            java.io.File file = new File(path.toString());
            if (!file.exists()) {
                file.mkdirs();
            }
            filePath = Paths.get(path.toString(), String.valueOf(Instant.now().toEpochMilli()) + jFileDO.getName()).toString();
        } else {
            java.io.File file = new File(dir.toString());
            if (!file.exists()) {
                file.mkdirs();
            }
            filePath = Paths.get(dir, jFileDO.getName()).toString();

        }

        String bucket = jFileDO.getPath().split("/")[1];
        String name = jFileDO.getPath().split("/")[2];
        try {
            minioClient.downloadObject(DownloadObjectArgs.builder().bucket(bucket).object(name).filename(filePath).build());
        } catch (Exception e) {
            log.error("文件下载异常:" + e);
            throw new RuntimeException(e);
        }

        return filePath;
    }

    @Override
    public void downloadCutFile(List<CutFileVO> cutFileVOList, String dir) {
        java.io.File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        for (CutFileVO cutFileVO : cutFileVOList) {
            JFileDO jFileDO = jFileMapper.selectById(cutFileVO.getId());
            String bucket = jFileDO.getPath().split("/")[1];
            String name = jFileDO.getPath().split("/")[2];
            String filePath = Paths.get(dir, cutFileVO.getOriginName()).toString();
            try {
                minioClient.downloadObject(DownloadObjectArgs.builder().bucket(bucket).object(name).filename(filePath).build());
            } catch (Exception e) {
                log.error("文件下载异常:" + e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public List<JFileDO> createFiles(Map<Integer, List<String>> fileMap) {
        Map.Entry<Integer, List<String>> entry = fileMap.entrySet().iterator().next();
        // 切分文件id
        Integer fid = entry.getKey();
        List<String> filePath = entry.getValue();
        List<JFileDO> jFileDOS = new ArrayList<>();
        for (String path: filePath) {
            File file = new File(path);
            String timestamp = String.valueOf(Instant.now().toEpochMilli());
            String fileName = timestamp + "_" + file.getName();
            try {
                minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(new FileInputStream(file), file.length(), -1).build());
            } catch (Exception e) {
                log.error("文件上传异常:" + e.toString());
            }
            JFileDO jFileDO = new JFileDO();
            jFileDO.setName(file.getName());
            jFileDO.setPath("/" + bucketName + "/" + fileName);
            jFileDO.setUrl(endpoint + "/" + bucketName + "/" + fileName);
            jFileDO.setSize(file.length());
            jFileDO.setType(file.getName().substring(file.getName().lastIndexOf(".")+1));
            jFileDO.setOriginId(fid);
            jFileMapper.insert(jFileDO);
            JFileDO fileDO = jFileMapper.selectById(jFileDO.getId());
            jFileDOS.add(fileDO);
        }
        return jFileDOS;
    }

    @Override
    public JFileDO searchById(Integer id) {
        return jFileMapper.selectById(id);
    }

    @Override
    public Boolean needCut(String[] fileIds) {
        for (String id : fileIds) {
            JFileDO jFileDO = jFileMapper.selectById(Integer.valueOf(id));
            if (jFileDO.getCut()) {
                return true;
            }
        }
        return false;
    }


}
