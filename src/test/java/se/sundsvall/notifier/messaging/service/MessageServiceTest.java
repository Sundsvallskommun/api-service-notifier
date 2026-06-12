package se.sundsvall.notifier.messaging.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.notifier.messaging.api.model.request.MessageRequest;
import se.sundsvall.notifier.messaging.api.model.request.MessageRequestWithoutRecipient;
import se.sundsvall.notifier.messaging.api.model.request.MessageType;
import se.sundsvall.notifier.messaging.api.model.response.MessageRecipientResponse;
import se.sundsvall.notifier.messaging.api.model.response.MessageResponse;
import se.sundsvall.notifier.messaging.integration.db.EmployeeRepository;
import se.sundsvall.notifier.messaging.integration.db.MessageRecipientRepository;
import se.sundsvall.notifier.messaging.integration.db.MessageRepository;
import se.sundsvall.notifier.messaging.integration.db.model.EmployeeEntity;
import se.sundsvall.notifier.messaging.integration.db.model.MessageEntity;
import se.sundsvall.notifier.messaging.integration.db.model.MessageRecipientEntity;
import se.sundsvall.notifier.messaging.integration.smssender.MessageStatus;
import se.sundsvall.notifier.messaging.integration.smssender.SmsSenderIntegration;
import se.sundsvall.notifier.messaging.integration.teamssender.TeamsSenderIntegration;
import se.sundsvall.notifier.messaging.service.mapper.EntityToResponseMapper;
import se.sundsvall.notifier.messaging.service.mapper.MessageMapper;
import se.sundsvall.notifier.messaging.service.utility.PhoneNumberUtil;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

	@Mock
	private TeamsSenderIntegration teamsSenderIntegration;
	@Mock
	private SmsSenderIntegration smsSenderIntegration;
	@Mock
	private PhoneNumberUtil phoneNumberUtil;

	@Mock
	private MessageRepository messageRepository;
	@Mock
	private MessageRecipientRepository messageRecipientRepository;
	@Mock
	private EmployeeRepository employeeRepository;

	@Mock
	private MessageMapper messageMapper;
	@Mock
	private EntityToResponseMapper entityMapper;
	@InjectMocks
	private MessageService messageService;

	@Test
	void createMessagesTest() {
		var recipients = Set.of(1L, 2L);
		var messageRequest = new MessageRequest(
			"title",
			"content",
			"sender",
			recipients,
			MessageType.TEAMS_AND_SMS);
		MessageRecipientEntity recipient = new MessageRecipientEntity();

		var message = MessageEntity.builder().withId(1L).build();

		var employee1 = new EmployeeEntity();
		employee1.setId(1L);
		var employee2 = new EmployeeEntity();
		employee2.setId(2L);

		var employees = List.of(employee1, employee2);

		when(messageMapper.toEntity(any(MessageRequest.class))).thenReturn(message);
		when(messageRepository.save(any(MessageEntity.class))).thenReturn(message);
		when(employeeRepository.findAllById(recipients)).thenReturn(employees);
		when(messageMapper.toMessageRecipient(any(), any())).thenReturn(recipient);

		messageService.createMessage(messageRequest);

		verify(messageRepository, times(1)).save(any(MessageEntity.class));
		verify(employeeRepository).findAllById(recipients);
		verify(messageMapper, times(2)).toMessageRecipient(any(), any());
	}

	@Test
	void getMessageByIdTest() {
		var messageId = 1L;
		var email = "test@sundsvall.se";
		var message = MessageEntity.builder().withId(messageId).withSender(email).build();
		var response = MessageResponse.builder().withId(messageId).withSender(email).build();

		when(messageRepository.findBySenderAndId(email, messageId)).thenReturn(Optional.of(message));
		when(messageMapper.entityToMessageResponse(message)).thenReturn(response);

		var result = messageService.getMessageById(email, messageId);
		assertThat(result).isEqualTo(response);
		verify(messageRepository).findBySenderAndId(email, messageId);
	}

	@Test
	void getMessagesByIdTest_NotFound() {
		var messageId = 1L;
		var email = "test";

		when(messageRepository.findBySenderAndId(any(), any())).thenReturn(Optional.empty());
		var exception = assertThrows(Throwable.class, () -> messageService.getMessageById(email, messageId));

		verify(messageRepository).findBySenderAndId(any(), any());
		Assertions.assertThat(exception)
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Message with id: " + messageId + " not found");
		verifyNoInteractions(messageMapper);
	}

	@Test
	void getMessagesTest() {
		var sender = "sender";
		var message1 = MessageEntity.builder().withId(1L).build();
		var message2 = MessageEntity.builder().withId(2L).build();
		var messages = List.of(message1, message2);

		var response1 = MessageResponse.builder().withId(1L).build();
		var response2 = MessageResponse.builder().withId(2L).build();

		when(messageRepository.findAllBySender(sender)).thenReturn(messages);
		when(messageMapper.entityToMessageResponse(message1)).thenReturn(response1);
		when(messageMapper.entityToMessageResponse(message2)).thenReturn(response2);

		var result = messageService.getMessages(sender);

		assertThat(result).hasSize(2);
		assertThat(result).containsExactly(response1, response2);
		verify(messageRepository).findAllBySender(sender);
		verify(messageMapper, times(2)).entityToMessageResponse(any());
	}

	@Test
	void sendMessageTest() {
		EmployeeEntity employee = new EmployeeEntity();
		employee.setEmail("test@example.com");
		employee.setWorkMobile("+46701234567");
		MessageType messageStatus = MessageType.TEAMS_AND_SMS;
		String content = "this is message test";

		when(phoneNumberUtil.cleanPhoneNumber(anyString()))
			.thenReturn("+46701234567");
		when(teamsSenderIntegration.sendTeamsMessage(anyString(), any()))
			.thenReturn(true);
		when(smsSenderIntegration.sendSms(anyString(), any()))
			.thenReturn(MessageStatus.SENT);

		var result = messageService.sendMessageToEmployee(employee, messageStatus, content);

		assertThat(result).isEqualTo(MessageRecipientEntity.DeliveryStatus.DELIVERED);
		verify(smsSenderIntegration).sendSms(anyString(), any());
		verify(phoneNumberUtil).cleanPhoneNumber(anyString());
		verify(teamsSenderIntegration).sendTeamsMessage(anyString(), any());
	}

	@Test
	void sendMessageToAll_shouldProcessAllEmployeesAcrossPages() {
		EmployeeEntity employee1 = new EmployeeEntity();
		employee1.setEmail("test1@example.com");
		employee1.setWorkMobile("+46701234567");

		EmployeeEntity employee2 = new EmployeeEntity();
		employee2.setEmail("test2@example.com");
		employee2.setWorkMobile("+46707654321");

		var messageRequest = new MessageRequestWithoutRecipient(
			"title",
			"content",
			"sender",
			MessageType.TEAMS_AND_SMS);

		var savedMessage = MessageEntity.builder()
			.withTitle("title")
			.withContent("content")
			.withSender("sender")
			.withMessageType(MessageType.TEAMS_AND_SMS)
			.build();

		var recipient1 = new MessageRecipientEntity();
		var recipient2 = new MessageRecipientEntity();

		Page<EmployeeEntity> firstPage = new PageImpl<>(
			List.of(employee1),
			PageRequest.of(0, 200),
			201);

		Page<EmployeeEntity> secondPage = new PageImpl<>(
			List.of(employee2),
			PageRequest.of(1, 200),
			201);

		when(employeeRepository.findByActiveEmployeeTrue(PageRequest.of(0, 200)))
			.thenReturn(firstPage);
		when(employeeRepository.findByActiveEmployeeTrue(PageRequest.of(1, 200)))
			.thenReturn(secondPage);
		when(messageRepository.save(any(MessageEntity.class)))
			.thenReturn(savedMessage);

		when(messageMapper.toMessageRecipient(employee1, MessageRecipientEntity.DeliveryStatus.DELIVERED))
			.thenReturn(recipient1);
		when(messageMapper.toMessageRecipient(employee2, MessageRecipientEntity.DeliveryStatus.DELIVERED))
			.thenReturn(recipient2);
		when(phoneNumberUtil.cleanPhoneNumber(anyString()))
			.thenAnswer(invocation -> invocation.getArgument(0));
		when(teamsSenderIntegration.sendTeamsMessage(anyString(), any()))
			.thenReturn(true);
		when(smsSenderIntegration.sendSms(anyString(), any()))
			.thenReturn(MessageStatus.SENT);

		messageService.sendMessageToAll(messageRequest);

		verify(messageRepository).save(any(MessageEntity.class));
		verify(employeeRepository).findByActiveEmployeeTrue(PageRequest.of(0, 200));
		verify(employeeRepository).findByActiveEmployeeTrue(PageRequest.of(1, 200));

		verify(teamsSenderIntegration, times(2)).sendTeamsMessage(anyString(), any());
		verify(smsSenderIntegration, times(2)).sendSms(anyString(), any());
		verify(phoneNumberUtil, times(2)).cleanPhoneNumber(anyString());

		verify(messageMapper).toMessageRecipient(employee1, MessageRecipientEntity.DeliveryStatus.DELIVERED);
		verify(messageMapper).toMessageRecipient(employee2, MessageRecipientEntity.DeliveryStatus.DELIVERED);
		verify(messageRecipientRepository).save(recipient1);
		verify(messageRecipientRepository).save(recipient2);

		assertThat(recipient1.getMessage()).isEqualTo(savedMessage);
		assertThat(recipient2.getMessage()).isEqualTo(savedMessage);
	}

	@Test
	void getRecipientsWithMessageId() {

		Long messageId = 1L;
		Pageable pageable = PageRequest.of(0, 2);

		MessageRecipientEntity entity = new MessageRecipientEntity();
		MessageRecipientResponse response = mock(MessageRecipientResponse.class);
		Page<MessageRecipientEntity> entityPage = new PageImpl<>(List.of(entity));

		when(messageRecipientRepository.findByMessageId(messageId, pageable))
			.thenReturn(entityPage);

		when(messageMapper.mapToRecipientResponse(entity))
			.thenReturn(response);

		Page<MessageRecipientResponse> result = messageService.getRecipientsWithMessageId(messageId, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().getFirst()).isEqualTo(response);

		verify(messageRecipientRepository).findByMessageId(messageId, pageable);
		verify(messageMapper).mapToRecipientResponse(entity);
	}
}
