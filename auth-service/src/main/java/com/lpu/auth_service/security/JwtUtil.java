package com.lpu.auth_service.security;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
// This utility class is responsible for generating and validating JWT tokens.
@Component
public class JwtUtil {
	// The secret key in Auth Service and API Gateway is the same to ensure that both services can generate and validate the same tokens.
	private final String SECRET = "mySuperSecretKeyThatIsAtLeast32CharactersLong!!";
	private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes()); //HMACSHA  + HS256

	// This method generates a JWT token containing the user's email and role as claims.
	public String generateToken(String email, String role) {
		return Jwts.builder()
				.setSubject(email)
				.claim("role", role)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))  //1 hour
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}
	// This method validates the JWT token by parsing it with the secret key. 
	// If the token is valid, it returns true; otherwise, it returns false.
	private Claims getClaims(String token) {
	    return Jwts.parserBuilder()
	            .setSigningKey(key)
	            .build()
	            .parseClaimsJws(token)
	            .getBody();
	}

	public String extractEmail(String token) {
	    return getClaims(token).getSubject();
	}

	public String extractRole(String token) {
	    return getClaims(token).get("role", String.class);
	}
}
