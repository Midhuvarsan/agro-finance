package com.agrofinance.entity;
 
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
 
/**
 * A single land parcel belonging to a farmer.
 * Owning side of the Farmer <-> Land one-to-many (holds farmer_id).
 */
@Entity
@Table(name = "land")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Land extends Auditable {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;
 
    @Column(name = "survey_number", nullable = false)
    private String surveyNumber;
 
    /** BigDecimal, not double — avoids binary floating-point rounding error. */
    @Column(name = "area_acres", precision = 10, scale = 2)
    private BigDecimal areaAcres;
 
    private String location;
 
    @Column(name = "soil_type")
    private String soilType;
 
    /** Same composition reasoning as Farmer.lands — a crop-planting event has no life apart from its land. */
    @OneToMany(mappedBy = "land", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Crop> crops = new ArrayList<>();
 
}
 