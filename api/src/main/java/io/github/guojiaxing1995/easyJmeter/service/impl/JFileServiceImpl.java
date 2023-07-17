package io.github.guojiaxing1995.easyJmeter.service.impl;

import io.github.guojiaxing1995.easyJmeter.mapper.JFileMapper;
import io.github.guojiaxing1995.easyJmeter.model.JFileDO;
import io.github.guojiaxing1995.easyJmeter.service.JFileService;
import io.github.talelin.autoconfigure.exception.FailedException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.Date;

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
    public JFileDO createFile(MultipartFile file) {
        String name = file.getOriginalFilename();
        String fileExtension = StringUtils.getFilenameExtension(name);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = dateFormat.format(new Date());
        String objectName = timestamp + "." + fileExtension;
        try {
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(file.getInputStream(), file.getSize(), -1).build());
        } catch (Exception e) {
            e.printStackTrace();
            throw new FailedException(12101);
        }
        JFileDO jFileDO = new JFileDO();
        jFileDO.setName(name);
        jFileDO.setPath("/" + bucketName + "/" + objectName);
        jFileDO.setUrl(endpoint + "/" + bucketName + "/" + objectName);
        jFileDO.setSize(file.getSize());
        jFileDO.setType(file.getContentType());
        jFileMapper.insert(jFileDO);

        return jFileMapper.selectById(jFileDO.getId());
    }
}
