package se.sundsvall.notifier.messaging.integration.db;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.sundsvall.notifier.messaging.integration.db.model.GroupEntity;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
	List<GroupEntity> findAllByCreatorId(String creatorId);
}
