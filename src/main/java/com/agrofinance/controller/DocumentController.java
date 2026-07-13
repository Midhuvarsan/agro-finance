package com.agrofinance.controller;
 
import com.agrofinance.dto.DocumentResponse;
import com.agrofinance.entity.DocumentType;
import com.agrofinance.security.CustomUserDetails;
import com.agrofinance.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
 
import java.util.List;
 
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
 
    private final DocumentService documentService;
 
    /**
     * consumes = MULTIPART_FORM_DATA: file uploads arrive as multipart
     * form fields, not a JSON body — so @RequestParam, not @RequestBody.
     */
    @PreAuthorize("hasRole('FARMER')")
    @PostMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadMyDocument(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam("type") DocumentType type,
            @RequestParam("file") MultipartFile file
    ) {
        DocumentResponse response = documentService.upload(principal.getUser().getId(), type, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
 
    @PreAuthorize("hasRole('FARMER')")
    @GetMapping("/me")
    public List<DocumentResponse> listMyDocuments(@AuthenticationPrincipal CustomUserDetails principal) {
        return documentService.listMyDocuments(principal.getUser().getId());
    }
 
}
 