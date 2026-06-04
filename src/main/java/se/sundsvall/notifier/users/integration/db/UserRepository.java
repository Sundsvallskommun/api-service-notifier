package se.sundsvall.notifier.users.integration.db;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.notifier.users.integration.db.model.UserEntity;
import se.sundsvall.notifier.users.integration.db.model.enums.Role;

public interface UserRepository extends JpaRepository<UserEntity, String> {

	Optional<UserEntity> findByEmail(String email);

	Optional<UserEntity> findById(Long id);

	List<UserEntity> findAllByRole(Role role);

	void deleteByEmail(String email);

	void deleteById(Long Id);
}
