package com.kami.cloud.lease.common.minio;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author kami
 * @description
 * @createDate 2026-07-17 15:11
 */
@Configuration
@EnableConfigurationProperties(MinIOProperties.class)
@ConditionalOnProperty(name = "minio.endpoint")
public class MinIOConfiguration {
    @Autowired
    private MinIOProperties minioProperties;

    @Bean
    public MinioClient minioClient (){
        return MinioClient.builder().endpoint(minioProperties.getEndpoint()).credentials(minioProperties.getAccessKey(),minioProperties.getSecretKey()).build();
    }
}
