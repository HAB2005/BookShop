package com.example.system_backend.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.upload")
public class FileUploadProperties {
    
    /**
     * Directory where uploaded files will be stored
     */
    private String dir = "uploads";
    
    /**
     * Maximum file size in bytes (default: 5MB)
     */
    private long maxFileSize = 5242880L; // 5MB
}