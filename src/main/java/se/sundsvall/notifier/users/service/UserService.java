package se.sundsvall.notifier.users.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.dept44.problem.*;
import se.sundsvall.notifier.users.api.model.UpdateUserRequest;
import se.sundsvall.notifier.users.api.model.UserRequest;
import se.sundsvall.notifier.users.api.model.UserResponse;
import se.sundsvall.notifier.users.integration.db.UserRepository;
import se.sundsvall.notifier.users.integration.db.model.enums.Role;
import se.sundsvall.notifier.users.integration.db.model.enums.Status;
import se.sundsvall.notifier.users.service.mapper.UserMapper;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional
public class UserService {

	private final UserRepository userRepository;

	private final UserMapper userMapper;

	private final PasswordEncoder passwordEncoder;

	private final String USER_NOT_FOUND = "user %s was not found";

	public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
		this.passwordEncoder = passwordEncoder;
	}

	public UserResponse createUser(UserRequest userRequest) {

		if (userRepository.findByEmail(userRequest.getEmail()).isEmpty()) {
			String hashedPassword = passwordEncoder.encode(userRequest.getPassword());
			final var userEntity = userRepository.save(userMapper.toUserEntity(userRequest, hashedPassword));
			return userMapper.toUserResponse(userEntity);
		}
		String USER_ALREADY_EXISTING = "user already exists";
		throw Problem.valueOf(CONFLICT, format(USER_ALREADY_EXISTING));
	}

	// READ
	public UserResponse getUserByEmail(String email) {
		return userRepository.findByEmail(email).map(userMapper::toUserResponse)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(USER_NOT_FOUND, email)));
	}

	public UserResponse getUserById(Long id) {
		return userRepository.findById(id).map(userMapper::toUserResponse)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(USER_NOT_FOUND, id)));
	}

	// UPDATE
	public void updateUserPasswordById(Long id, String password) {
		var userEntity = userRepository.findById(id)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(USER_NOT_FOUND, id)));
		userEntity.setPassword(passwordEncoder.encode(password));
		userRepository.save(userEntity);
	}

	public UserResponse updateUserById(UpdateUserRequest userRequest, Long id) {

		var userEntity = userRepository.findById(id)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(USER_NOT_FOUND, id)));

		var updated = userEntity
			.withId(id)
			.withEmail(userRequest.getEmail())
			.withPhoneNumber(userRequest.getPhoneNumber())
			.withMunicipalityId(userMapper.resolveMunicipalityId(userRequest.getMunicipalityName()))
			.withStatus(Status.valueOf(userRequest.getStatus().toUpperCase()));

		if (userRequest.getRole() != null) {
			updated = updated.withRole(Role.valueOf(userRequest.getRole().toUpperCase()));
		}

		userRepository.save(updated);

		return userMapper.toUserResponse(userEntity);
	}

	// DELETE
	public void deleteUserByEmail(String email) {
		var userEntity = userRepository.findByEmail(email)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(USER_NOT_FOUND, email)));
		if (userEntity.getRole() == Role.ADMIN) {
			throw Problem.valueOf(org.springframework.http.HttpStatus.FORBIDDEN, "admin users cannot be deleted");
		}
		userRepository.deleteByEmail(email);
	}

	public void deleteUserById(Long id) {
		var userEntity = userRepository.findById(id)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(USER_NOT_FOUND, id)));
		if (userEntity.getRole() == Role.ADMIN) {
			throw Problem.valueOf(org.springframework.http.HttpStatus.FORBIDDEN, "admin users cannot be deleted");
		}
		userRepository.deleteById(id);
	}

	public List<UserResponse> getAllUsers() {
		return userRepository.findAllByRole(Role.USER).stream()
			.map(userMapper::toUserResponse)
			.collect(Collectors.toList());
	}
}
