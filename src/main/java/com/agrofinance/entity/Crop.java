package com.agrofinance.entity;
 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
import java.time.LocalDate;
 
/**
 * A crop-planting event: one crop, on one land parcel, in one season.
 * Owning side of two relationships: Land <-> Crop and CropType <-> Crop.
 */
@Entity
@Table(name = "crop")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Crop extends Auditable {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "land_id", nullable = false)
    private Land land;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_type_id", nullable = false)
    private CropType cropType;
 
    private String season;
 
    /** LocalDate (date only) — not LocalDateTime — these are calendar dates, not timestamped events. */
    @Column(name = "sowing_date")
    private LocalDate sowingDate;
 
    @Column(name = "expected_harvest_date")
    private LocalDate expectedHarvestDate;
 
}
 