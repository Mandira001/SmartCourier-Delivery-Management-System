package com.lpu.api_gateway.security;

import java.security.Key;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	// This creates the signing key for JWT validation. In production, this should be stored securely and not hardcoded.
	private final String SECRET = "mySuperSecretKeyThatIsAtLeast32CharactersLong!!";
	// HS256 requires a key of at least 256 bits (32 bytes), so we ensure our secret is long enough. The Keys.hmacShaKeyFor method converts the string into a Key object suitable for HMAC signing and verification.
	private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes()); //HMACSHA  + HS256

	private Claims getClaims(String token) {
	    return Jwts.parserBuilder()
		        // This sets the signing key for JWT validation. The same key must be used for both signing and validation.
	            .setSigningKey(key)
	            .build()
				// This parses and verifies the token. If token is invalid, expired, or signed with a different secret, parsing fails.
	            .parseClaimsJws(token)
	            .getBody();
	}

	// These methods extract specific claims (email and role) from the JWT. The email is typically stored in the "sub" (subject) claim, while the role is stored in a custom claim named "role".
	public String extractEmail(String token) {
	    return getClaims(token).getSubject();
	}

	public String extractRole(String token) {
	    return getClaims(token).get("role", String.class);
	}
}
