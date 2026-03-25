package com.lpu.auth_service.security;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	private final String SECRET = "mySuperSecretKeyThatIsAtLeast32CharactersLong!!";
	private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes()); //HMACSHA  + HS256

	public String generateToken(String email, String role) {
		return Jwts.builder()
				.setSubject(email)
				.claim("role", role)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))  //1 hour
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}
	
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
