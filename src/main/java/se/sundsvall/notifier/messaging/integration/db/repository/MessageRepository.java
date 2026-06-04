package se.sundsvall.notifier.messaging.integration.db.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.notifier.messaging.integration.db.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
	List<Message> findAllBySender(String sender);

	Optional<Message> findBySenderAndId(String sender, Long messageId);
}
