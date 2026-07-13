package com.agrofinance.dto;
 
import com.agrofinance.entity.DocumentType;
 
import java.time.LocalDateTime;
 
public record DocumentResponse(
        Long id,
        DocumentType documentType,
        String fileName,
        boolean verified,
        LocalDateTime uploadedAt
) {
}
 