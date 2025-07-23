package com.example.Authentication.Controllers;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Authentication.Entity.AuthResponse;
import com.example.Authentication.Entity.LoginRequest;
import com.example.Authentication.Entity.RegisterRequest;
import com.example.Authentication.Entity.RoleUpdate;
import com.example.Authentication.Entity.UserEntity;
import com.example.Authentication.Repository.UserRepository;
import com.example.Authentication.Security.JwtUtil;
import com.example.Authentication.ServiceImpl.CustomerUserDetailService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UserRepository userRepository;
	private final AuthenticationManager authenticationManager;
	private final CustomerUserDetailService customerUserDetailService;
	private final JwtUtil jwtUtil;
	private final PasswordEncoder passwordEncoder;

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest userResponse) {
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(userResponse.getUsername(), userResponse.getPassword()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong username or password!");
		}

		UserDetails userDetails = customerUserDetailService.loadUserByUsername(userResponse.getUsername());
		String token = jwtUtil.generateToken(userDetails);
		
		Set<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.toSet());

		return ResponseEntity.ok(new AuthResponse(token, userResponse.getUsername(), roles));
	}


	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
		if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already taken");
		}

		UserEntity newUser = new UserEntity();
		newUser.setUsername(registerRequest.getUsername());
		newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
		newUser.setAuthorities(Set.of("ROLE_USER"));

		userRepository.save(newUser);

		// Authenticate to create context
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(registerRequest.getUsername(), registerRequest.getPassword()));

		UserDetails userDetails = customerUserDetailService.loadUserByUsername(registerRequest.getUsername());
		String token = jwtUtil.generateToken(userDetails);

		Set<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.toSet());

		return ResponseEntity.ok(new AuthResponse(token, newUser.getUsername(), roles));
	}

	@PostMapping("/register-admin")
	public ResponseEntity<?> registerAdmin(@RequestBody RegisterRequest request) {
		if (userRepository.findByUsername(request.getUsername()).isPresent()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already taken");
		}

		UserEntity userEntity = new UserEntity();
		userEntity.setUsername(request.getUsername());
		userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
		userEntity.setAuthorities(Set.of("ROLE_ADMIN"));

		userRepository.save(userEntity);

		authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

		UserDetails userByUsername = customerUserDetailService.loadUserByUsername(request.getUsername());
		String token = jwtUtil.generateToken(userByUsername);

		Set<String> roles = userByUsername.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.toSet());

		return ResponseEntity.ok(new AuthResponse(token, userEntity.getUsername(), roles));

	}

	@PutMapping("/get/{username}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')") // ✅ Only admin can promote roles
	public ResponseEntity<?> promote(@PathVariable String username, @RequestBody RoleUpdate update) {
		Optional<UserEntity> byUsername = userRepository.findByUsername(username); // ✅ Use path variable

		if (byUsername.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}

		UserEntity userEntity = byUsername.get();
		userEntity.setAuthorities(update.getRoles());
		userRepository.save(userEntity);

		return ResponseEntity.ok("User roles updated successfully");
	}


}
