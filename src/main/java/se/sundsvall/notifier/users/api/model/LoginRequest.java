package se.sundsvall.notifier.users.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

	@Schema(description = "Epost-adress", example = "kalle.kula@sundsvall.se")
	@Email(message = "must be a valid Email-adress", regexp = "^[A-Za-zÅÄÖåäö0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
	private String email;

	@Schema(description = "Lösenord", example = "mittLösenord")
	@NotBlank
	private String password;

	public static LoginRequest create() {
		return new LoginRequest();
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public LoginRequest withEmail(String email) {
		setEmail(email);
		return this;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public LoginRequest withPassword(String password) {
		setPassword(password);
		return this;
	}
}
