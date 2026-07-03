package se.sundsvall.notifier.users.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

public class UserRequest extends BaseUserRequest {

	@Schema(description = "Epost-adress", example = "kalle.kula@sundsvall.se")
	@Email(message = "must be a valid Email-adress", regexp = "^[A-Za-zÅÄÖåäö0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
	@NotBlank(message = "cannot be blank")
	private String email;

	@Schema(description = "Lösenord", example = "mittLösenord")
	@NotBlank
	private String password;

	public static UserRequest create() {
		return new UserRequest();
	}

	public UserRequest() {
		setRole("USER");
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public UserRequest withEmail(String email) {
		this.email = email;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public UserRequest withPassword(String password) {
		this.password = password;
		return this;
	}

	public UserRequest withPhoneNumber(String phoneNumber) {
		setPhoneNumber(phoneNumber);
		return this;
	}

	public UserRequest withMunicipalityName(String municipalityName) {
		setMunicipalityName(municipalityName);
		return this;
	}

	public UserRequest withStatus(String status) {
		setStatus(status);
		return this;
	}

	public UserRequest withRole(String role) {
		setRole(role);
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), email);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		var that = (UserRequest) o;
		return super.equals(o) && Objects.equals(email, that.email);
	}

	@Override
	public String toString() {
		return "UserRequest{" +
			"email='" + email + '\'' +
			", password='" + password + '\'' +
			'}';
	}
}
