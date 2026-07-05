package com.agrofinance.entity;
 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
import java.math.BigDecimal;
import java.time.LocalDate;
 
/**
 * Crop price history by crop type and market. Append-only fact table —
 * a recorded price is never edited, so no Auditable/updatedAt here.
 * Composite index matches the planned query pattern: trend lookups
 * filter by crop type AND a date range together.
 */
@Entity
@Table(
        name = "market_price",
        indexes = @Index(name = "idx_market_price_croptype_date", columnList = "crop_type_id, recorded_date")
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MarketPrice {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_type_id", nullable = false)
    private CropType cropType;
 
    @Column(name = "market_location")
    private String marketLocation;
 
    @Column(name = "price_per_unit", precision = 10, scale = 2)
    private BigDecimal pricePerUnit;
 
    @Column(name = "recorded_date", nullable = false)
    private LocalDate recordedDate;
 
}
 