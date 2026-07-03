package se.sundsvall.notifier.messaging.integration.db;

import com.google.code.beanmatchers.BeanMatchers;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.notifier.messaging.integration.db.model.GroupEntity;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsFor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeFor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.AllOf.allOf;

public class GroupEntityTest {

	@BeforeAll
	static void setUp() {
		BeanMatchers.registerValueGenerator(
			() -> LocalDateTime.now().minusDays(1),
			LocalDateTime.class);
	}

	@Test
	void testBean() {
		org.hamcrest.MatcherAssert.assertThat(GroupEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanEqualsFor("id"),
			hasValidBeanHashCodeFor("id"),
			hasValidBeanToStringExcluding("employees")));
	}

	@Test
	void onCreate_setsCreatedAt_setsUpdatedAt_whenNull() {
		var group = new GroupEntity();
		group.setCreatedAt(null);
		group.setUpdatedAt(null);

		group.onCreate();

		assertThat(group.getCreatedAt()).isNotNull();
		assertThat(group.getUpdatedAt()).isNotNull();
	}

	@Test
	void onUpdate_updatesUpdatedAt() {
		var group = new GroupEntity();
		var oldUpdated = LocalDateTime.now().minusDays(1);
		group.setUpdatedAt(oldUpdated);

		group.onUpdate();

		assertThat(group.getUpdatedAt()).isAfter(oldUpdated);
	}

}
