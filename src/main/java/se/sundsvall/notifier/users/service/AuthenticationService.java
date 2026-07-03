package se.sundsvall.notifier.users.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.problem.*;
import se.sundsvall.notifier.security.JwtUtil;
import se.sundsvall.notifier.users.api.model.JwtResponse;
import se.sundsvall.notifier.users.api.model.LoginRequest;
import se.sundsvall.notifier.users.integration.db.UserRepository;
import se.sundsvall.notifier.users.integration.db.model.enums.Status;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthenticationService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtService;

	public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	public JwtResponse login(LoginRequest loginRequest) {
		var user = userRepository.findByEmail(loginRequest.getEmail())
			.orElseThrow(() -> Problem.valueOf(UNAUTHORIZED, "Invalid credentials"));

		if (!passwordEncoder.matches(loginRequest.getPassword(), (user.getPassword()))) {
			throw Problem.valueOf(UNAUTHORIZED, "Invalid credentials");
		}

		if (Status.SUSPENDED == user.getStatus()) {
			throw Problem.valueOf(FORBIDDEN, "Account suspended");
		}

		if (Status.INACTIVE == user.getStatus()) {
			throw Problem.valueOf(FORBIDDEN, "Account inactive");
		}

		var token = jwtService.generateToken(user.getEmail(), user.getRole().name());
		return new JwtResponse(token);
	}

}
