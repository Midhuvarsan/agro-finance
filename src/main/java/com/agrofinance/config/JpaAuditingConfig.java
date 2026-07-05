package com.agrofinance.config;
 
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
 
/**
 * Master switch for JPA auditing.
 *
 * Without @EnableJpaAuditing here, the @CreatedDate / @LastModifiedDate
 * annotations on Auditable do nothing — they are inert until this
 * configuration class registers the auditing infrastructure with Spring.
 *
 * This class deliberately has no body: its only job is to carry
 * the annotation. This is a common, valid pattern for "switch-flipping"
 * configuration classes in Spring.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
 









