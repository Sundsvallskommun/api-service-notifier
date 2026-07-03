package se.sundsvall.notifier.messaging.integration.db;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.notifier.messaging.integration.db.model.MessageEntity;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
	List<MessageEntity> findAllBySender(String sender);

	Optional<MessageEntity> findBySenderAndId(String sender, Long messageId);
}
