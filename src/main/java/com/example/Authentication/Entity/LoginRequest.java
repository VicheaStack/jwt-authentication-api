package com.example.Authentication.Entity;

import lombok.Data;

@Data
public class LoginRequest {
	private String username;
	private String password;
}
