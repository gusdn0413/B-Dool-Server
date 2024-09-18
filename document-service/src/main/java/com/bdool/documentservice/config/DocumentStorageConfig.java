package com.bdool.documentservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentStorageConfig {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Bean
    public String fileUploadDir() {
        return uploadDir;
    }
}
