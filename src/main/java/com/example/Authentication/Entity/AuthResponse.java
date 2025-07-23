package com.example.Authentication.Entity;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
	private String token;
	private String username;
	private Set<String> roles;
}

