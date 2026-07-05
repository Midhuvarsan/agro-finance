package com.agrofinance.security;
 
import com.agrofinance.entity.User;
import com.agrofinance.entity.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
 
import java.util.Collection;
import java.util.stream.Collectors;
 
/**
 * Wraps our User entity to satisfy Spring Security's UserDetails contract,
 * without making User itself implement a framework interface. Same
 * separation-of-concerns principle we applied to Farmer/BankOfficer.
 */
public class CustomUserDetails implements UserDetails {
 
    private final User user;
 
    public CustomUserDetails(User user) {
        this.user = user;
    }
 
    /** Exposes the wrapped entity when a service needs the real User, not just Spring Security's view of it. */
    public User getUser() {
        return user;
    }
 
    @Override
    public String getUsername() {
        return user.getEmail();
    }
 
    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }
 
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ROLE_ prefix added HERE, not stored in the database — Spring
        // Security's hasRole("FARMER") silently looks for "ROLE_FARMER".
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toSet());
    }
 
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
 
    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != UserStatus.SUSPENDED;
    }
 
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
 
    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE && user.getDeletedAt() == null;
    }
 
}