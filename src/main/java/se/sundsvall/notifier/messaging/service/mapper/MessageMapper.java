package se.sundsvall.notifier.messaging.service.mapper;

import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import se.sundsvall.notifier.messaging.api.model.request.MessageRequest;
import se.sundsvall.notifier.messaging.api.model.request.Priority;
import se.sundsvall.notifier.messaging.api.model.response.MessageRecipientResponse;
import se.sundsvall.notifier.messaging.api.model.response.MessageResponse;
import se.sundsvall.notifier.messaging.api.model.response.MessageWithRecipientsResponse;
import se.sundsvall.notifier.messaging.integration.db.model.EmployeeEntity;
import se.sundsvall.notifier.messaging.integration.db.model.MessageEntity;
import se.sundsvall.notifier.messaging.integration.db.model.MessageRecipientEntity;
import se.sundsvall.notifier.messaging.integration.smssender.SmsDto;
import se.sundsvall.notifier.messaging.integration.teamssender.TeamsSenderDTO;

@Component
public class MessageMapper {

	public MessageEntity toEntity(MessageRequest messageRequest) {
		return MessageEntity.builder()
			.withTitle(messageRequest.title())
			.withContent(messageRequest.content())
			.withSender(messageRequest.sender())
			.withMessageType(messageRequest.messageType())
			.build();
	}

	public MessageResponse entityToMessageResponse(MessageEntity message) {
		if (message == null) {
			return null;
		}

		return MessageResponse.builder()
			.withId(message.getId())
			.withTitle(message.getTitle())
			.withContent(message.getContent())
			.withSender(message.getSender())
			.withMessageType(message.getMessageType())
			.withCreatedAt(message.getCreatedAt())
			.build();

	}

	public MessageWithRecipientsResponse entityToMessageWithRecipientsResponse(MessageEntity message) {
		if (message == null) {
			return null;
		}
		var recipients = message.getRecipients().stream()
			.map(recipient -> MessageRecipientResponse.builder()
				.withEmployeeId(recipient.getEmployee().getId())
				.withFirstName(recipient.getEmployee().getFirstName())
				.withLastName(recipient.getEmployee().getLastName())
				.withWorkTitle(recipient.getWorkTitle())
				.withOrgId(recipient.getOrgId())
				.withOrgName(recipient.getEmployee().getOrganization().getName())
				.withDeliveryStatus(recipient.getDeliveryStatus().toString())
				.build()).collect(Collectors.toSet());

		return MessageWithRecipientsResponse.builder()
			.withId(message.getId())
			.withTitle(message.getTitle())
			.withContent(message.getContent())
			.withSender(message.getSender())
			.withMessageType(message.getMessageType())
			.withCreatedAt(message.getCreatedAt())
			.withRecipients(recipients)
			.build();

	}

	public SmsDto toSendSmsDto(String content, String mobileNumber) {

		return SmsDto.builder()
			.withMessage(content)
			.withMobileNumber(mobileNumber)
			.withPriority(Priority.HIGH)
			.build();
	}

	public TeamsSenderDTO toSendTeamsDto(String messageContent, String email) {
		return TeamsSenderDTO.builder()
			.withMessage(messageContent)
			.withRecipient(email)
			.build();
	}

	public MessageRecipientEntity toMessageRecipient(EmployeeEntity employee, MessageRecipientEntity.DeliveryStatus deliveryStatus) {
		return MessageRecipientEntity.builder()
			.withEmployee(employee)
			.withDeliveryStatus(deliveryStatus)
			.build();
	}

	public MessageRecipientResponse mapToRecipientResponse(MessageRecipientEntity messageRecipient) {
		return MessageRecipientResponse.builder()
			.withEmployeeId(messageRecipient.getEmployee().getId())
			.withFirstName(messageRecipient.getEmployee().getFirstName())
			.withLastName(messageRecipient.getEmployee().getLastName())
			.withOrgId(messageRecipient.getOrgId())
			.withOrgName(messageRecipient.getEmployee().getOrganization().getName())
			.withWorkTitle(messageRecipient.getEmployee().getWorkTitle())
			.withDeliveryStatus(messageRecipient.getDeliveryStatus().toString())
			.withReceivedAt(messageRecipient.getReceivedAt())
			.build();
	}

}
