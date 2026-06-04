package se.sundsvall.notifier.messaging.integration.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Entity
@Table(name = "message_recipient")
public class MessageRecipient {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "message_id", nullable = false)
	private Message message;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id", nullable = false)
	private Employee employee;

	@Column(name = "org_id", nullable = false)
	private String orgId;

	@Column(name = "work_title", length = 100)
	private String workTitle;

	@Column(name = "received_at", nullable = false)
	private LocalDateTime receivedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "delivery_status", nullable = false, length = 10)
	private DeliveryStatus deliveryStatus = DeliveryStatus.DELIVERED;

	public enum DeliveryStatus {
		DELIVERED, FAILED
	}

	@PrePersist
	public void onCreate() {
		if (receivedAt == null) {
			receivedAt = LocalDateTime.now();
		}
		if (orgId == null && employee != null && employee.getOrgId() != null) {
			this.orgId = employee.getOrgId();
		}
		if (workTitle == null && employee != null) {
			this.workTitle = employee.getWorkTitle();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof MessageRecipient that))
			return false;
		return id != null && id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "MessageRecipient{" +
			"id=" + id +
			", orgId=" + orgId +
			", workTitle='" + workTitle + '\'' +
			", receivedAt=" + receivedAt +
			", deliveryStatus=" + deliveryStatus +
			'}';
	}
}
