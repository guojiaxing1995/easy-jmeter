package io.github.guojiaxing1995.easyJmeter.service.impl;

import io.github.guojiaxing1995.easyJmeter.mapper.JFileMapper;
import io.github.guojiaxing1995.easyJmeter.model.JFileDO;
import io.github.guojiaxing1995.easyJmeter.service.JFileService;
import io.github.guojiaxing1995.easyJmeter.vo.JFileVO;
import io.github.talelin.autoconfigure.exception.FailedException;
import io.github.talelin.autoconfigure.exception.NotFoundException;
import io.github.talelin.autoconfigure.exception.ParameterException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Arrays;

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
}
