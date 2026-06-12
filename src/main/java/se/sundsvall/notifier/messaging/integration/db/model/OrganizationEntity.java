package se.sundsvall.notifier.messaging.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "organization")
public class OrganizationEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_org_id", referencedColumnName = "org_id", insertable = false, updatable = false)
	private OrganizationEntity parent;

	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
	private Set<OrganizationEntity> children = new HashSet<>();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "company_id", length = 64, nullable = false)
	private String companyId;

	@Column(name = "parent_org_id", length = 64, insertable = false, updatable = false)
	private String parentOrgId;

	@Column(name = "org_id", length = 64, unique = true, nullable = false)
	private String orgId;

	@Column(name = "org_name", nullable = false)
	private String name;

	@Column(name = "tree_level", nullable = false)
	private int treeLevel;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof OrganizationEntity that))
			return false;
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "OrganizationEntity{" +
			"id=" + id +
			", companyId='" + companyId + '\'' +
			", parentOrgId='" + parentOrgId + '\'' +
			", orgId='" + orgId + '\'' +
			", name='" + name + '\'' +
			", treeLevel=" + treeLevel +
			", createdAt=" + createdAt +
			", updatedAt=" + updatedAt +
			'}';
	}
}
