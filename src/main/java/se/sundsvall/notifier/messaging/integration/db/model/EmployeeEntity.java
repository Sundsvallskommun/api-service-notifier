package se.sundsvall.notifier.messaging.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "employee",
	uniqueConstraints = {
		@UniqueConstraint(name = "uq_person_id_org_id_work_title", columnNames = {
			"person_id", "org_id", "work_title"
		})
	})
public class EmployeeEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "org_id", referencedColumnName = "org_id", insertable = false, updatable = false)
	private OrganizationEntity organization;

	@ManyToMany(mappedBy = "employees", fetch = FetchType.LAZY)
	private Set<GroupEntity> groups = new HashSet<>();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "employee_id")
	private Long id;

	@Column(name = "person_id", length = 36, nullable = false)
	private String personId;

	@Column(name = "org_id", length = 64, nullable = false, insertable = false, updatable = false)
	private String orgId;

	@Column(name = "first_name", length = 100)
	private String firstName;

	@Column(name = "last_name", length = 100)
	private String lastName;

	@Column(name = "email", length = 255)
	private String email;

	@Column(name = "work_mobile", length = 50)
	private String workMobile;

	@Column(name = "work_phone", length = 50)
	private String workPhone;

	@Column(name = "work_title", length = 100)
	private String workTitle;

	@Column(name = "active_employee", nullable = false)
	private boolean activeEmployee = true;

	@Column(name = "manager_id", length = 36)
	private String managerPersonId;

	@Column(name = "manager_code", length = 10)
	private String managerCode;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof EmployeeEntity employee))
			return false;
		return id != null && Objects.equals(id, employee.id);
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "EmployeeEntity{" +
			"id=" + id +
			", personId='" + personId + '\'' +
			", orgId='" + orgId + '\'' +
			", firstName='" + firstName + '\'' +
			", lastName='" + lastName + '\'' +
			", email='" + email + '\'' +
			", workMobile='" + workMobile + '\'' +
			", workPhone='" + workPhone + '\'' +
			", workTitle='" + workTitle + '\'' +
			", activeEmployee=" + activeEmployee +
			", managerPersonId='" + managerPersonId + '\'' +
			", managerCode='" + managerCode + '\'' +
			", createdAt=" + createdAt +
			", updatedAt=" + updatedAt +
			'}';
	}
}
