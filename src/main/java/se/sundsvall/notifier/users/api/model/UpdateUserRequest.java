package se.sundsvall.notifier.users.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

public class UpdateUserRequest extends BaseUserRequest {

	@Schema(description = "E-postadress", example = "")
	@NotBlank(message = "cannot be blank")
	@Email
	private String email;

	public static UpdateUserRequest create() {
		return new UpdateUserRequest();
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public UpdateUserRequest withEmail(String email) {
		this.email = email;
		return this;
	}

	public UpdateUserRequest withPhoneNumber(String phoneNumber) {
		setPhoneNumber(phoneNumber);
		return this;
	}

	public UpdateUserRequest withMunicipalityName(String municipalityName) {
		setMunicipalityName(municipalityName);
		return this;
	}

	public UpdateUserRequest withStatus(String status) {
		setStatus(status);
		return this;
	}

	public UpdateUserRequest withRole(String role) {
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
		var that = (UpdateUserRequest) o;
		return super.equals(o) && Objects.equals(email, that.email);
	}

	@Override
	public String toString() {
		return "UpdateUserRequest{" +
			"email='" + email + '\'' +
			'}';
	}
}
