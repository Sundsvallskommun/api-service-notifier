package se.sundsvall.notifier.users.integration.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.notifier.users.integration.db.model.UserEntity;
import se.sundsvall.notifier.users.integration.db.model.enums.Status;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/script/truncate.sql",
	"/db/script/UserRepositoryTest.sql"
})
class UserRepositoryTest {

	private static final String MAIL_ADRESS_1 = "testmail1@sundsvall.se";
	private static final String MAIL_ADRESS_2 = "testmail2@sundsvall.se";
	private static final String PHONE_NUMBER_1 = "0011234567";
	private static final String PHONE_NUMBER_2 = "0021234567";
	private static final String MUNICIPALITY_ID_1 = "2281";
	private static final String MUNICIPALITY_ID_2 = "0689";
	private static final Status STATUS_1 = Status.ACTIVE;
	private static final Status STATUS_2 = Status.SUSPENDED;

	@Autowired
	private UserRepository userRepository;

	@Test
	void createUser() {
		final var userEntity = UserEntity.create()
			.withEmail("Test@testmail.com")
			.withPhoneNumber("0701740679")
			.withMunicipalityId("2281")
			.withStatus(Status.INACTIVE)
			.withPassword("$2a$10$nsmcrgNIVycw.1acUPPmJesm6lU7tdEhU6vlkDZDoGs31/cMI61hG");

		final var savedEntity = userRepository.save(userEntity);
		final var parsedEntity = userRepository.findByEmail(savedEntity.getEmail());

		assertThat(savedEntity.getId()).isNotNull();
		assertThat(savedEntity.getEmail()).isEqualTo("Test@testmail.com");
		assertThat(savedEntity.getPhoneNumber()).isEqualTo("0701740679");
		assertThat(savedEntity.getMunicipalityId()).isEqualTo("2281");
		assertThat(savedEntity.getStatus()).isEqualTo(Status.INACTIVE);
		assertThat(parsedEntity).isPresent();
	}

	@Test
	void updateUser() {
		final var userEntity = UserEntity.create()
			.withId(1L)
			.withEmail(MAIL_ADRESS_1).withPhoneNumber(PHONE_NUMBER_1)
			.withMunicipalityId(MUNICIPALITY_ID_1)
			.withStatus(STATUS_1)
			.withPassword("$2a$10$nsmcrgNIVycw.1acUPPmJesm6lU7tdEhU6vlkDZDoGs31/cMI61hG");

		final var savedEntity = userRepository.save(userEntity);
		assertThat(savedEntity.getEmail()).isEqualTo(MAIL_ADRESS_1);
		assertThat(savedEntity.getPhoneNumber()).isEqualTo(PHONE_NUMBER_1);
		assertThat(savedEntity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID_1);
		assertThat(savedEntity.getStatus()).isSameAs(STATUS_1);
		assertThat(savedEntity.getPhoneNumber()).isNotEqualTo(PHONE_NUMBER_2);
		assertThat(savedEntity.getMunicipalityId()).isNotEqualTo(MUNICIPALITY_ID_2);
		assertThat(savedEntity.getStatus()).isNotSameAs(STATUS_2);

		savedEntity.setPhoneNumber(PHONE_NUMBER_2);
		savedEntity.setMunicipalityId(MUNICIPALITY_ID_2);
		savedEntity.setStatus(STATUS_2);

		final var updatedEntity = userRepository.save(savedEntity);
		assertThat(updatedEntity.getId()).isEqualTo(savedEntity.getId());
		assertThat(updatedEntity.getEmail()).isEqualTo(MAIL_ADRESS_1);
		assertThat(updatedEntity.getPhoneNumber()).isEqualTo(PHONE_NUMBER_2);
		assertThat(updatedEntity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID_2);
		assertThat(updatedEntity.getStatus()).isSameAs(STATUS_2);
		assertThat(updatedEntity.getPhoneNumber()).isNotEqualTo(PHONE_NUMBER_1);
		assertThat(updatedEntity.getMunicipalityId()).isNotEqualTo(MUNICIPALITY_ID_1);
		assertThat(updatedEntity.getStatus()).isNotSameAs(STATUS_1);
	}

	@Test
	void getUserByMail() {
		final var testUser = userRepository.findByEmail(MAIL_ADRESS_1);

		assertThat(testUser).isPresent().hasValueSatisfying(user -> {
			assertThat(user.getEmail()).isEqualTo(MAIL_ADRESS_1);
			assertThat(user.getPhoneNumber()).isEqualTo(PHONE_NUMBER_1);
			assertThat(user.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID_1);
			assertThat(user.getStatus()).isEqualTo(STATUS_1);
		});
	}

	@Test
	@Transactional
	void deleteUser() {
		assertThat(userRepository.findByEmail(MAIL_ADRESS_2)).isPresent();
		userRepository.deleteByEmail(MAIL_ADRESS_2);
		assertThat(userRepository.findByEmail(MAIL_ADRESS_2)).isNotPresent();
	}
}
