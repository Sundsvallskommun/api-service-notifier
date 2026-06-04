package se.sundsvall.notifier.messaging.integration.db.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.sundsvall.notifier.messaging.integration.db.entity.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
	List<Group> findAllByCreatorId(String creatorId);
}
