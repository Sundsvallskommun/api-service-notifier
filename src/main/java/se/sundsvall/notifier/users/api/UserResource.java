package se.sundsvall.notifier.users.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import se.sundsvall.dept44.problem.*;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.notifier.users.api.model.UpdateUserRequest;
import se.sundsvall.notifier.users.api.model.UserRequest;
import se.sundsvall.notifier.users.api.model.UserResponse;
import se.sundsvall.notifier.users.service.UserService;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping(path = "/api", produces = APPLICATION_PROBLEM_JSON_VALUE)
@Validated
@Tag(name = "Users", description = "Resources for handling Users")
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "500", description = "Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "503", description = "Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
public class UserResource {
	private final UserService userService;
	private static final String PATH = "/api/users/";

	public UserResource(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("users")
	@Operation(summary = "Create a user")
	@ApiResponse(responseCode = "201", description = "Successful operation", useReturnTypeSchema = true)
	@ApiResponse(responseCode = "409", description = "Already exists", useReturnTypeSchema = true)
	public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserRequest userRequest) {
		final var user = userService.createUser(userRequest);
		return ResponseEntity.created(UriComponentsBuilder.fromPath(PATH).buildAndExpand(userRequest).toUri())
			.body(user);
	}

	@GetMapping("users/emails/{email}")
	@Operation(summary = "Get information about a user with Email")
	public ResponseEntity<UserResponse> getUserByEmail(@Parameter(description = "Email-address") @Valid @Email @PathVariable String email) {
		var user = userService.getUserByEmail(email);
		return user != null ? ok(user) : ResponseEntity.noContent().build();
	}

	@GetMapping("users/ids/{id}")
	@Operation(summary = "Get information about the user with Id")
	public ResponseEntity<UserResponse> getUserById(
		@Parameter(description = "Id") @Valid @PathVariable Long id) {
		var user = userService.getUserById(id);
		return user != null ? ok(user) : ResponseEntity.noContent().build();
	}

	@PatchMapping("users/ids/{id}")
	@Operation(summary = "Update information of a user with Id")
	@ApiResponse(responseCode = "201", description = "Successful operation", useReturnTypeSchema = true)
	@Validated
	public ResponseEntity<UserResponse> updateUserById(
		@Parameter(description = "Id") @PathVariable Long id, @RequestBody @Valid UpdateUserRequest userRequest) {
		var user = userService.updateUserById(userRequest, id);
		return ResponseEntity.created(UriComponentsBuilder.fromPath(PATH).buildAndExpand(userRequest).toUri())
			.body(user);
	}

	@DeleteMapping("users/emails/{email}")
	@Operation(summary = "Delete a user with Email")
	@ApiResponse(responseCode = "204", description = "Successful operation", useReturnTypeSchema = true)
	public ResponseEntity<Void> deleteByEmail(
		@Parameter(description = "Email-address") @Valid @Email @PathVariable String email) {
		userService.deleteUserByEmail(email);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("users/ids/{id}")
	@Operation(summary = "Delete a user with Id")
	@ApiResponse(responseCode = "204", description = "Successful operation", useReturnTypeSchema = true)
	public ResponseEntity<Void> deleteById(
		@Parameter(description = "Id") @Valid @PathVariable Long id) {
		userService.deleteUserById(id);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("users/ids/{id}/password")
	@Operation(summary = "Update a users password with id")
	@ApiResponse(responseCode = "204", description = "Successful operation", useReturnTypeSchema = true)
	public ResponseEntity<Void> updateUserPasswordById(@PathVariable Long id, @RequestBody String password) {
		userService.updateUserPasswordById(id, password);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Get all users")
	@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	@GetMapping("users")
	public ResponseEntity<List<UserResponse>> getAllUsers() {
		return ResponseEntity.ok(userService.getAllUsers());
	}

}
