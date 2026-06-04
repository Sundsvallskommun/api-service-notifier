package se.sundsvall.notifier.users.integration.db.model;

import jakarta.persistence.*;
import se.sundsvall.notifier.users.integration.db.model.enums.Role;
import se.sundsvall.notifier.users.integration.db.model.enums.Status;

@Entity
@Table(name = "users",
	indexes = {
		@Index(name = "idx_email_address", columnList = "email_address", unique = true)
	})
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, name = "id")
	private Long Id;

	@Column(nullable = false, name = "email_address", unique = true)
	private String email;

	@Column(nullable = false, name = "phone_number")
	private String phoneNumber;

	@Column(nullable = false, name = "municipality_id")
	private String municipalityId;

	@Column(nullable = false, name = "status")
	@Enumerated(EnumType.STRING)
	private Status status;

	@Column(nullable = false, name = "password")
	private String password;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Role role = Role.USER;

	public static UserEntity create() {
		return new UserEntity();
	}

	public Long getId() {
		return Id;
	}

	public void setId(Long id) {
		this.Id = id;
	}

	public UserEntity withId(Long id) {
		this.Id = id;
		return this;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public UserEntity withEmail(String email) {
		this.email = email;
		return this;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public UserEntity withPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
		return this;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public UserEntity withMunicipalityId(String municipalityId) {
		this.municipalityId = municipalityId;
		return this;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public UserEntity withStatus(Status status) {
		this.status = status;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public UserEntity withPassword(String password) {
		this.password = password;
		return this;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public UserEntity withRole(Role role) {
		this.role = role;
		return this;
	}
}
