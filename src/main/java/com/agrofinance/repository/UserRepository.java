package com.agrofinance.repository;
 
import com.agrofinance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.Optional;
 
/**
 * No implementation class needed — Spring Data JPA generates one at
 * runtime via a dynamic proxy. JpaRepository<User, Long> already
 * provides save(), findById(), findAll(), delete(), etc.
 */
public interface UserRepository extends JpaRepository<User, Long> {
 
    /**
     * Query derived entirely from the method name: Spring parses
     * "findByEmail" and generates the equivalent of
     * SELECT u FROM User u WHERE u.email = :email — no JPQL written by hand.
     */
    Optional<User> findByEmail(String email);
 
    /**
     * Translates to an EXISTS/COUNT-style query — avoids fetching the
     * full entity just to check for a duplicate email at registration time.
     */
    boolean existsByEmail(String email);
 
}
 
