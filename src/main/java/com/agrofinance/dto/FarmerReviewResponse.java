package com.agrofinance.dto;
 
import java.util.List;
 
/**
 * Composite read-model for loan reviewers: the farmer's profile plus
 * everything relevant to a credit decision, in one response. Reuses
 * the existing per-domain DTOs rather than inventing new shapes.
 */
public record FarmerReviewResponse(
        FarmerResponse profile,
        List<LandResponse> lands,
        List<CropResponse> crops,
        List<DocumentResponse> documents
) {
}
 


























