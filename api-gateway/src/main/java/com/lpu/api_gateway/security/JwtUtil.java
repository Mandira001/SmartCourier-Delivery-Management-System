package com.lpu.api_gateway.security;

import java.security.Key;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	private final String SECRET = "mySuperSecretKeyThatIsAtLeast32CharactersLong!!";
	private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes()); //HMACSHA  + HS256

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
