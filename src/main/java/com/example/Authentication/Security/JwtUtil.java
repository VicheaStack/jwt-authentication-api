package com.example.Authentication.Security;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.example.Authentication.ServiceImpl.CustomerUserDetailService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys; // ✅ You must include this!

@Component
public class JwtUtil {

	private CustomerUserDetailService userDetailService;
	private final String secretKey = "mySecretKeyIsNotLookLikewhatyouthinkitgonnabe";
	private final Long EXPIRATION_DATE = 1000L * 60 * 60;

	private final SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());

	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	public String generateToken(UserDetails userDetails) {
		List<String> roles = userDetails.getAuthorities().stream().map(authority -> authority.getAuthority())
				.collect(Collectors.toList());

		return Jwts.builder().subject(userDetails.getUsername()).claim("roles", roles) // store the roles in the JWT
				.issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + EXPIRATION_DATE)).signWith(key)
				.compact();
	}

	public boolean validateToken(String token, String username) {
		try {
			String extractedUsername = extractUsername(token);
			return extractedUsername.equals(username) && !isTokenExpired(token);
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isTokenExpired(String token) {
		return extractAllClaims(token).getExpiration().before(new Date());
	}

	private Claims extractAllClaims(String token) {
		try {
			JwtParser parser = Jwts.parser().verifyWith(key).build();

			return parser.parseSignedClaims(token).getPayload();
		} catch (Exception e) {
			throw new RuntimeException("Invalid", e);
		}
	}

	public List<SimpleGrantedAuthority> extractAuthorities(String token) {
		List<String> roles = extractAllClaims(token).get("roles", List.class);
		return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
	}

}