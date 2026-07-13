package com.agrofinance.service.impl;
 
import com.agrofinance.config.FileStorageProperties;
import com.agrofinance.dto.DocumentResponse;
import com.agrofinance.entity.Document;
import com.agrofinance.entity.DocumentType;
import com.agrofinance.entity.Farmer;
import com.agrofinance.repository.DocumentRepository;
import com.agrofinance.repository.FarmerRepository;
import com.agrofinance.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
 
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;
 
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
 
    /** Whitelist, not blacklist — everything not explicitly allowed is rejected. */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf", "image/jpeg", "image/png"
    );
 
    private final DocumentRepository documentRepository;
    private final FarmerRepository farmerRepository;
    private final FileStorageProperties storageProperties;
 
    @Override
    @Transactional
    public DocumentResponse upload(Long farmerUserId, DocumentType type, MultipartFile file) {
 
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }
        if (file.getSize() > storageProperties.getMaxFileSize()) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "File exceeds the maximum allowed size");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Only PDF, JPEG, and PNG files are allowed");
        }
 
        Farmer farmer = farmerRepository.findById(farmerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Complete your profile first"));
 
        // SERVER generates the stored filename (UUID + safe extension).
        // The client's original filename is never used as a disk path —
        // that's the classic path-traversal vulnerability (e.g. a file
        // named "../../etc/passwd").
        String extension = switch (file.getContentType()) {
            case "application/pdf" -> ".pdf";
            case "image/jpeg" -> ".jpg";
            default -> ".png";
        };
        String storedName = UUID.randomUUID() + extension;
 
        try {
            Path uploadRoot = Paths.get(storageProperties.getUploadDir()).toAbsolutePath().normalize();
            Files.createDirectories(uploadRoot);
            Path target = uploadRoot.resolve(storedName);
            file.transferTo(target.toFile());
 
            Document document = new Document();
            document.setFarmer(farmer);
            document.setDocumentType(type);
            document.setFilePath(target.toString());
            document.setVerified(false);
 
            Document saved = documentRepository.save(document);
            return toResponse(saved);
 
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file");
        }
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> listMyDocuments(Long farmerUserId) {
        return documentRepository.findByFarmerUserId(farmerUserId).stream()
                .map(this::toResponse)
                .toList();
    }
 
    private DocumentResponse toResponse(Document doc) {
        String fileName = Paths.get(doc.getFilePath()).getFileName().toString();
        return new DocumentResponse(
                doc.getId(),
                doc.getDocumentType(),
                fileName,
                doc.isVerified(),
                doc.getCreatedAt()
        );
    }
 
}
 












