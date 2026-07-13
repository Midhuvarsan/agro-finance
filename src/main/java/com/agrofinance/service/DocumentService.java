package com.agrofinance.service;
 
import com.agrofinance.dto.DocumentResponse;
import com.agrofinance.entity.DocumentType;
import org.springframework.web.multipart.MultipartFile;
 
import java.util.List;
 
public interface DocumentService {
 
    DocumentResponse upload(Long farmerUserId, DocumentType type, MultipartFile file);
 
    List<DocumentResponse> listMyDocuments(Long farmerUserId);
 
}
 












