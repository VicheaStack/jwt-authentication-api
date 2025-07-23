package com.example.Authentication.Entity;

import java.util.Set;

import lombok.Data;

@Data
public class RoleUpdate {
	private String username;
	private Set<String> roles;
}
