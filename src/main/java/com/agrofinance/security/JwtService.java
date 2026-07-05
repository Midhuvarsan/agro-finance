package com.agrofinance.security;
 
import com.agrofinance.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
 
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
 
/**
 * Generates and validates JWTs. HS256 (symmetric signing) is the right
 * choice here since one service both creates and verifies tokens — no
 * need for RS256's public/private key split, which matters only when a
 * separate service must verify tokens without being able to create them.
 */
@Service
@RequiredArgsConstructor
public class JwtService {
 
    private final JwtProperties jwtProperties;
 
    /**
     * Builds a signed token. Subject = email (our login identifier);
     * roles are embedded as a custom claim so the frontend can read
     * them without a separate API call, even though (see class notes
     * in JwtAuthenticationFilter) we still re-verify against the
     * database on every request rather than trusting the claim alone.
     */
    public String generateToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
 
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpiration());
 
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }
 
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
 
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
 
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
 
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
 
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
 
    private SecretKey getSigningKey() {
        // Keys.hmacShaKeyFor requires >= 256 bits — a short/weak secret
        // throws WeakKeyException at runtime, not compile time.
        byte[] keyBytes = java.util.Base64.getDecoder().decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
 
}
