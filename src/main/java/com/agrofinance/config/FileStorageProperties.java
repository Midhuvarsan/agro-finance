package com.agrofinance.config;
 
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
 
/**
 * Same @ConfigurationProperties pattern as JwtProperties (Phase 3).
 * Local disk for now; swapping to S3/cloud storage later only changes
 * the service implementation, not callers — the reason file storage
 * logic is isolated behind its own service.
 */
@Component
@ConfigurationProperties(prefix = "storage")
@Getter
@Setter
public class FileStorageProperties {
 
    /** Directory where uploaded documents are written. */
    private String uploadDir = "uploads";
 
    /** Max file size in bytes we accept at the service level (5 MB default). */
    private long maxFileSize = 5 * 1024 * 1024;
 
}
 












