package com.agrofinance.repository;
 
import com.agrofinance.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.Optional;
 
public interface RoleRepository extends JpaRepository<Role, Long> {
 
    /** Used during registration to attach the correct Role row (e.g. "FARMER") to a new User. */
    Optional<Role> findByName(String name);
 
}