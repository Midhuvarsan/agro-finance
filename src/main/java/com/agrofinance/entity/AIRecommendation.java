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
 
import java.math.BigDecimal;
 
/**
 * One Gemini API call's output for a loan. A Loan can accumulate several
 * of these over time (e.g. re-assessment after new documents are
 * uploaded), so this is a one-to-many from Loan, not a one-to-one.
 *
 * geminiModelVersion is recorded for reproducibility: AI model behavior
 * changes over time, so knowing which version produced a given
 * recommendation matters for explaining past decisions later.
 */
@Entity
@Table(name = "ai_recommendation")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class AIRecommendation extends Auditable {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;
 
    @Column(name = "risk_score", precision = 5, scale = 2)
    private BigDecimal riskScore;
 
    @Column(name = "recommendation_text", columnDefinition = "TEXT")
    private String recommendationText;
 
    @Column(name = "gemini_model_version")
    private String geminiModelVersion;
 
}