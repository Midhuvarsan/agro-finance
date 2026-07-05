package com.agrofinance.entity;
 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
/**
 * Lookup table for crop names/categories (e.g. "Wheat" / "Cereal").
 * Exists to satisfy 3NF: without this, Crop would store crop name and
 * category as free text, and category would transitively depend on
 * crop name rather than on the Crop row's own primary key.
 */
@Entity
@Table(name = "crop_type")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CropType {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
 
    @Column(nullable = false, unique = true)
    private String name;
 
    private String category;
 
}