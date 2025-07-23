package com.example.Authentication.ServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.Authentication.Entity.UserEntity;
import com.example.Authentication.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CustomerUserDetailService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<UserEntity> userOpt = userRepository.findByUsername(username);

		if (userOpt.isEmpty()) {
			throw new UsernameNotFoundException("Not Found! " + username);
		}

		UserEntity userEntity = userOpt.get();

		List<SimpleGrantedAuthority> authorities = userEntity.getAuthorities().stream().map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());

		return User.builder().username(userEntity.getUsername()).password(userEntity.getPassword())
				.authorities(authorities).build();
	}

}
