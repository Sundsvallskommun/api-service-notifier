package se.sundsvall.notifier.messaging.integration.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.notifier.messaging.integration.db.model.MessageRecipientEntity;

public interface MessageRecipientRepository extends JpaRepository<MessageRecipientEntity, Long> {
	Page<MessageRecipientEntity> findByMessageId(Long messageId, Pageable pageable);
}
