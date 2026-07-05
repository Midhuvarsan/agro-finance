package com.agrofinance.security;
 
import com.agrofinance.entity.User;
import com.agrofinance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
 
/**
 * The one method Spring Security actually needs implemented.
 * "username" here is our email — Spring Security's naming is generic
 * regardless of which field you actually use as the login identifier.
 *
 * @RequiredArgsConstructor (Lombok): generates a constructor for all
 * final fields — this IS constructor injection, just without typing
 * the constructor by hand. Equivalent to what we wrote manually in
 * Phase 1's DI explanation.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
 
    private final UserRepository userRepository;
 
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));
        return new CustomUserDetails(user);
    }
 
}
