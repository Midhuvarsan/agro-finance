package com.agrofinance.dto;
 
import org.springframework.data.domain.Page;
 
import java.util.List;
 
/**
 * Our own pagination envelope instead of returning Spring's Page/PageImpl
 * directly — exposing framework internals in an API contract couples
 * clients to Spring's serialization format (which Spring itself warns
 * about since 3.3). This record IS our contract.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
 


























