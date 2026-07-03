package se.sundsvall.notifier.messaging.integration.db;

import com.google.code.beanmatchers.BeanMatchers;
import java.time.LocalDateTime;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.notifier.messaging.integration.db.model.MessageEntity;
import se.sundsvall.notifier.messaging.integration.db.model.MessageRecipientEntity;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsFor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeFor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.AllOf.allOf;

public class MessageEntityTest {

	@BeforeAll
	static void setUp() {
		BeanMatchers.registerValueGenerator(
			() -> LocalDateTime.now().minusDays(1),
			LocalDateTime.class);
	}

	@Test
	void testBean() {
		org.hamcrest.MatcherAssert.assertThat(MessageEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanEqualsFor("id"),
			hasValidBeanHashCodeFor("id"),
			hasValidBeanToStringExcluding("recipients", "group", "sender", "messageType")));
	}

	@Test
	void testBuilderMethods() {

		final var title = "title";
		final var content = "content";
		final var sender = "sender";

		var message = MessageEntity.builder()
			.withTitle(title)
			.withContent(content)
			.withSender(sender)
			.build();

		assertThat(message.getTitle()).isEqualTo(title);
		assertThat(message.getContent()).isEqualTo(content);
		assertThat(message.getSender()).isEqualTo(sender);
	}

	@Test
	void onCreate_createdAt_null() {
		var message = new MessageEntity();
		message.setCreatedAt(null);

		message.onCreate();

		assertThat(message.getCreatedAt()).isNotNull();
	}

	@Test
	void onCreate_doesNotChangeWhenSet() {
		var message = new MessageEntity();
		var t = LocalDateTime.now().minusDays(5);
		message.setCreatedAt(t);

		message.onCreate();

		assertThat(message.getCreatedAt()).isEqualTo(t);
	}

	@Test
	void addRecipient_addsAndLinks() {
		var message = new MessageEntity();
		message.setRecipients(new HashSet<>());

		var recipient = new MessageRecipientEntity();
		message.addRecipient(recipient);

		assertThat(message.getRecipients()).contains(recipient);
		assertThat(recipient.getMessage()).isSameAs(message);
	}

	@Test
	void addRecipient_null() {
		var message = new MessageEntity();
		message.setRecipients(new HashSet<>());

		message.addRecipient(null);

		assertThat(message.getRecipients()).isEmpty();
	}

	@Test
	void addRecipient_addedTwice() {
		var message = new MessageEntity();
		message.setRecipients(new HashSet<>());

		var recipient = new MessageRecipientEntity();

		message.addRecipient(recipient);
		message.addRecipient(recipient);

		assertThat(message.getRecipients()).hasSize(1);
	}
}
