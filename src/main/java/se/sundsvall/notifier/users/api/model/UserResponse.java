package se.sundsvall.notifier.users.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

public class UserResponse {

	@Schema(description = "id", example = "123")
	private Long id;
	@Schema(description = "Epost-adress", example = "kalle.kula@sundsvall.se")
	private String email;
	@Schema(description = "Telefonnummer", example = "0701740669")
	private String phoneNumber;
	@Schema(description = "Kommunnamn", example = "Sundsvall")
	private String municipalityName;
	@Schema(description = "Status", example = "ACTIVE")
	private String status;
	@Schema(description = "Roll", example = "USER")
	private String role;

	public static UserResponse create() {
		return new UserResponse();
	}

	public String getMunicipalityName() {
		return municipalityName;
	}

	public void setMunicipalityName(String municipalityName) {
		this.municipalityName = municipalityName;
	}

	public UserResponse withMunicipalityName(String municipalityName) {
		this.municipalityName = municipalityName;
		return this;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UserResponse withId(Long id) {
		this.id = id;
		return this;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public UserResponse withEmail(String email) {
		this.email = email;
		return this;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public UserResponse withPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
		return this;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public UserResponse withStatus(String status) {
		this.status = status;
		return this;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public UserResponse withRole(String role) {
		this.role = role;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof final UserResponse that))
			return false;
		return Objects.equals(id, that.id) && Objects.equals(email, that.email) && Objects.equals(phoneNumber, that.phoneNumber) && Objects.equals(municipalityName, that.municipalityName) && Objects.equals(status, that.status) && Objects.equals(role,
			that.role);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, email, phoneNumber, municipalityName, status, role);
	}

	@Override
	public String toString() {
		return "UserResponse{" +
			"id=" + id +
			", email='" + email + '\'' +
			", phoneNumber='" + phoneNumber + '\'' +
			", municipalityName='" + municipalityName + '\'' +
			", status='" + status + '\'' +
			", role='" + role + '\'' +
			'}';
	}
}
