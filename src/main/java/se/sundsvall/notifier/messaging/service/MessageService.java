package se.sundsvall.notifier.messaging.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.notifier.messaging.api.model.request.MessageRequest;
import se.sundsvall.notifier.messaging.api.model.request.MessageRequestWithoutRecipient;
import se.sundsvall.notifier.messaging.api.model.request.MessageType;
import se.sundsvall.notifier.messaging.api.model.response.MessageRecipientResponse;
import se.sundsvall.notifier.messaging.api.model.response.MessageResponse;
import se.sundsvall.notifier.messaging.integration.db.entity.Employee;
import se.sundsvall.notifier.messaging.integration.db.entity.Message;
import se.sundsvall.notifier.messaging.integration.db.entity.MessageRecipient;
import se.sundsvall.notifier.messaging.integration.db.repository.EmployeeRepository;
import se.sundsvall.notifier.messaging.integration.db.repository.MessageRecipientRepository;
import se.sundsvall.notifier.messaging.integration.db.repository.MessageRepository;
import se.sundsvall.notifier.messaging.integration.smssender.MessageStatus;
import se.sundsvall.notifier.messaging.integration.smssender.SmsSenderIntegration;
import se.sundsvall.notifier.messaging.integration.teamssender.TeamsSenderIntegration;
import se.sundsvall.notifier.messaging.service.mapper.MessageMapper;
import se.sundsvall.notifier.messaging.service.utility.PhoneNumberUtil;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Service
public class MessageService {

	private final MessageRepository messageRepository;
	private final EmployeeRepository employeeRepository;
	private final MessageRecipientRepository messageRecipientRepository;
	private final MessageMapper messageMapper;
	private final SmsSenderIntegration smsSenderIntegration;
	private final TeamsSenderIntegration teamsSenderIntegration;
	private final PhoneNumberUtil phoneNumberUtil;

	public MessageService(MessageRepository messageRepository,
		EmployeeRepository employeeRepository, MessageRecipientRepository messageRecipientRepository,
		MessageMapper messageMapper, SmsSenderIntegration smsSenderIntegration,
		TeamsSenderIntegration teamsSenderIntegration, PhoneNumberUtil phoneNumberUtil) {
		this.messageRepository = messageRepository;
		this.employeeRepository = employeeRepository;
		this.messageRecipientRepository = messageRecipientRepository;
		this.messageMapper = messageMapper;
		this.smsSenderIntegration = smsSenderIntegration;
		this.teamsSenderIntegration = teamsSenderIntegration;
		this.phoneNumberUtil = phoneNumberUtil;
	}

	@Async
	public void createMessage(MessageRequest messageRequest) {

		var savedMessage = messageRepository.save(messageMapper.toEntity(messageRequest));

		var employees = employeeRepository.findAllById(messageRequest.recipientEmployeeIds());

		for (Employee employee : employees) {
			try {
				var delivered = sendMessageToEmployee(employee, messageRequest.messageType(), messageRequest.content());
				MessageRecipient messageRecipient = messageMapper.toMessageRecipient(employee, delivered);
				messageRecipient.setMessage(savedMessage);
				messageRecipientRepository.save(messageRecipient);
			} catch (Exception e) {
				log.error("Failed to send to employee {}", employee.getId(), e);
				MessageRecipient messageRecipient = messageMapper.toMessageRecipient(employee, MessageRecipient.DeliveryStatus.FAILED);
				messageRecipient.setMessage(savedMessage);
				messageRecipientRepository.save(messageRecipient);
			}
		}
	}

	@Async
	public void sendMessageToAll(MessageRequestWithoutRecipient messageRequest) {

		var message = Message.builder()
			.withTitle(messageRequest.title())
			.withContent(messageRequest.content())
			.withSender(messageRequest.sender())
			.withMessageType(messageRequest.messageType())
			.build();

		var savedMessage = messageRepository.save(message);

		int page = 0;
		int size = 200;
		int processed = 0;
		Page<Employee> employeePage;

		do {
			employeePage = employeeRepository.findByActiveEmployeeTrue(PageRequest.of(page, size));

			for (Employee employee : employeePage.getContent()) {
				try {
					var delivered = sendMessageToEmployee(employee, messageRequest.messageType(), messageRequest.content());
					var recipient = messageMapper.toMessageRecipient(employee, delivered);
					recipient.setMessage(savedMessage);
					messageRecipientRepository.save(recipient);
				} catch (Exception e) {
					var recipient = messageMapper.toMessageRecipient(employee, MessageRecipient.DeliveryStatus.FAILED);
					recipient.setMessage(savedMessage);
					messageRecipientRepository.save(recipient);
				}
			}
			processed += employeePage.getNumberOfElements();
			log.info("Messages processed: {}", processed);
			page++;

		} while (employeePage.hasNext());
	}

	public MessageResponse getMessageById(String sender, Long messageId) {
		var message = messageRepository.findBySenderAndId(sender, messageId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Message with id: " + messageId + " not found"));
		return messageMapper.entityToMessageResponse(message);
	}

	public List<MessageResponse> getMessages(String sender) {
		return messageRepository.findAllBySender(sender)
			.stream()
			.map(messageMapper::entityToMessageResponse)
			.toList();
	}

	public void deleteMessages(Long id) {
		messageRepository.deleteById(id);
	}

	public MessageRecipient.DeliveryStatus sendMessageToEmployee(Employee employee, MessageType messageType, String content) {
		boolean teamsSuccess = false;
		MessageStatus smsSuccess = MessageStatus.NOT_SENT;
		boolean isTeamsMessage = messageType == MessageType.TEAMS || messageType == MessageType.TEAMS_AND_SMS;
		boolean isSmsMessage = messageType == MessageType.SMS || messageType == MessageType.TEAMS_AND_SMS;

		if (isTeamsMessage && employee.getEmail() != null) {
			try {
				teamsSuccess = teamsSenderIntegration.sendTeamsMessage("2281",
					messageMapper.toSendTeamsDto(content, employee.getEmail()));
				// Throttling for TeamsSender to help it keep up and not timeout requests
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

		}

		String phoneNumber = phoneNumberUtil.cleanPhoneNumber(employee.getWorkMobile());
		if (isSmsMessage && phoneNumber != null) {
			smsSuccess = smsSenderIntegration.sendSms("2281",
				messageMapper.toSendSmsDto(content, phoneNumber));
		}
		return (teamsSuccess || smsSuccess == MessageStatus.SENT)
			? MessageRecipient.DeliveryStatus.DELIVERED
			: MessageRecipient.DeliveryStatus.FAILED;
	}

	public Page<MessageRecipientResponse> getRecipientsWithMessageId(Long id, Pageable pageable) {
		return messageRecipientRepository.findByMessageId(id, pageable).map(messageMapper::mapToRecipientResponse);
	}
}
