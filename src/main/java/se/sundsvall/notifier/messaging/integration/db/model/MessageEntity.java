package se.sundsvall.notifier.messaging.integration.db.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.sundsvall.notifier.messaging.api.model.request.MessageType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Entity
@Table(name = "message")
public class MessageEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "message_id")
	private Long id;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "content", columnDefinition = "TEXT", nullable = false)
	private String content;

	@Column(name = "sender", nullable = false)
	private String sender;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private Set<MessageRecipientEntity> recipients = new HashSet<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "message_type", nullable = false, length = 20)
	private MessageType messageType;

	@PrePersist
	public void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	public void addRecipient(MessageRecipientEntity recipient) {
		if (recipient == null)
			return;

		recipients.add(recipient);
		recipient.setMessage(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof MessageEntity message))
			return false;
		return id != null && Objects.equals(id, message.id);
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "MessageEntity{" +
			"id=" + id +
			", title='" + title + '\'' +
			", content='" + content + '\'' +
			", sender=" + (sender != null ? sender : "null") +
			", MessageType=" + messageType +
			", createdAt=" + createdAt +
			'}';
	}
}
