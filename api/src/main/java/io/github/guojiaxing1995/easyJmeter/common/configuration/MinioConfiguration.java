package io.github.guojiaxing1995.easyJmeter.common.configuration;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import io.minio.errors.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfiguration {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;

    private static final String PUBLIC_READ_POLICY_JSON_TEMPLATE = "{\n" +
            "  \"Version\": \"2012-10-17\",\n" +
            "  \"Statement\": [\n" +
            "    {\n" +
            "      \"Effect\": \"Allow\",\n" +
            "      \"Principal\": {\n" +
            "        \"AWS\": [\"*\"]\n" +
            "      },\n" +
            "      \"Action\": [\"s3:GetObject\"],\n" +
            "      \"Resource\": [\"arn:aws:s3:::%s/*\"]\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    @Bean
    public MinioClient minioClient() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        client.setTimeout(10000, 1800000,1800000);

        boolean isExists = client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!isExists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
        String formattedPolicyJson = String.format(PUBLIC_READ_POLICY_JSON_TEMPLATE, bucketName);
        client.setBucketPolicy(SetBucketPolicyArgs.builder()
                .bucket(bucketName)
                .config(formattedPolicyJson)
                .build());
        return client;
    }
}
