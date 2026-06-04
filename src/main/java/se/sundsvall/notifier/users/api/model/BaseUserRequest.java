package se.sundsvall.notifier.users.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;
import se.sundsvall.dept44.common.validators.annotation.ValidMobileNumber;
import se.sundsvall.notifier.users.api.validation.ValidEnum;
import se.sundsvall.notifier.users.api.validation.ValidMunicipalityName;
import se.sundsvall.notifier.users.integration.db.model.enums.Role;
import se.sundsvall.notifier.users.integration.db.model.enums.Status;

public abstract class BaseUserRequest {

	@Schema(description = "Telefonnummer", example = "0701740669")
	@NotBlank(message = "cannot be blank")
	@ValidMobileNumber(message = "must be a valid mobile number")
	private String phoneNumber;

	@Schema(description = "Kommunnamn", example = "Sundsvall")
	@NotBlank(message = "cannot be blank")
	@ValidMunicipalityName(message = "must be a valid municipality name")
	private String municipalityName;

	@Schema(description = "Status", example = "ACTIVE")
	@ValidEnum(message = "must be ACTIVE, INACTIVE or SUSPENDED", enumClass = Status.class, ignoreCase = true)
	private String status;

	@Schema(description = "Roll", example = "USER")
	@ValidEnum(message = "must be USER or ADMIN", enumClass = Role.class, ignoreCase = true)
	private String role;

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getMunicipalityName() {
		return municipalityName;
	}

	public void setMunicipalityName(String municipalityName) {
		this.municipalityName = municipalityName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public int hashCode() {
		return Objects.hash(phoneNumber, municipalityName, status, role);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		BaseUserRequest that = (BaseUserRequest) o;
		return Objects.equals(phoneNumber, that.phoneNumber)
			&& Objects.equals(municipalityName, that.municipalityName)
			&& Objects.equals(status, that.status)
			&& Objects.equals(role, that.role);
	}
}
