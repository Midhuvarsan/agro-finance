package com.agrofinance.repository;
 
import com.agrofinance.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.util.Optional;
 
public interface UserRepository extends JpaRepository<User, Long> {
 
    /**
     * JOIN FETCH here, not a plain derived query: roles is LAZY on the
     * entity (deliberately — see Phase 2), and with open-in-view: false
     * (deliberately — see Phase 1), the Hibernate session closes the
     * moment this method returns. Anything that later touches
     * user.getRoles() outside this method — like
     * JwtAuthenticationFilter calling getAuthorities() — would hit
     * LazyInitializationException without this explicit fetch join.
     * This keeps LAZY as the correct general-purpose default while
     * solving the one place we specifically know we need roles too.
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);
 
    boolean existsByEmail(String email);
 
    /**
     * @EntityGraph = "for THIS query, fetch roles eagerly" — an
     * alternative to JOIN FETCH that composes with Pageable.
     * Honest caveat: paginating while fetching a to-many collection
     * makes Hibernate paginate IN MEMORY (it logs HHH90003004) because
     * the joined rows can't be LIMITed correctly in SQL. Acceptable for
     * an admin screen over thousands of users; at real scale you'd
     * switch to a two-query pattern (page the ids, then fetch roles for
     * just that page).
     */
    @Override
    @EntityGraph(attributePaths = "roles")
    Page<User> findAll(Pageable pageable);
 
}
 
 


























