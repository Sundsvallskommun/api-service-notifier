package se.sundsvall.notifier.messaging.integration.db;

import com.google.code.beanmatchers.BeanMatchers;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.notifier.messaging.integration.db.entity.Employee;
import se.sundsvall.notifier.messaging.integration.db.entity.Message;
import se.sundsvall.notifier.messaging.integration.db.entity.MessageRecipient;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsFor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeFor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.AllOf.allOf;

class MessageRecipientEntityTest {

	@BeforeAll
	static void setUp() {
		BeanMatchers.registerValueGenerator(
			() -> LocalDateTime.now().minusDays(1),
			LocalDateTime.class);
	}

	@Test
	void testBean() {
		org.hamcrest.MatcherAssert.assertThat(MessageRecipient.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanEqualsFor("id"),
			hasValidBeanHashCodeFor("id"),
			hasValidBeanToStringExcluding("message", "employee")));
	}

	@Test
	void testBuilderMethods() {
		final var message = new Message();
		final var employee = new Employee();
		final var orgId = "orgId";
		final var workTitle = "workTitle";
		final var receivedAt = LocalDateTime.now();
		final var deliveryStatus = MessageRecipient.DeliveryStatus.DELIVERED;

		final var messageRecipient = MessageRecipient.builder()
			.withOrgId(orgId)
			.withWorkTitle(workTitle)
			.withReceivedAt(receivedAt)
			.withDeliveryStatus(deliveryStatus)
			.withEmployee(employee)
			.withMessage(message)
			.build();

		assertThat(messageRecipient.getMessage()).isSameAs(message);
		assertThat(messageRecipient.getEmployee()).isSameAs(employee);
		assertThat(messageRecipient.getOrgId()).isEqualTo(orgId);
		assertThat(messageRecipient.getWorkTitle()).isEqualTo(workTitle);
		assertThat(messageRecipient.getReceivedAt()).isEqualTo(receivedAt);
		assertThat(messageRecipient.getDeliveryStatus()).isEqualTo(deliveryStatus);
	}

	@Test
	void onCreate_setsReceivedAtWhenNull() {
		var recipient = new MessageRecipient();
		recipient.setReceivedAt(null);

		recipient.onCreate();

		assertThat(recipient.getReceivedAt()).isNotNull();
	}

	@Test
	void onCreate_doesNotChangeWhenSet() {
		var recipient = new MessageRecipient();
		var t = LocalDateTime.now().minusDays(3);
		recipient.setReceivedAt(t);

		recipient.onCreate();

		assertThat(recipient.getReceivedAt()).isEqualTo(t);
	}
}
