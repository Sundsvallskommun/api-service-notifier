package se.sundsvall.notifier.messaging.integration.smssender;

import generated.se.sundsvall.smssender.SendSmsRequest;
import generated.se.sundsvall.smssender.SendSmsRequest.PriorityEnum;
import generated.se.sundsvall.smssender.Sender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SmsSenderIntegrationMapper {
	@Value("${integration.sms-sender.sender}")
	private String sender;

	public SendSmsRequest toSendSmsRequest(final SmsDto dto) {
		if (dto == null) {
			return null;
		}

		return new SendSmsRequest()
			.sender(new Sender().name(sender))
			.mobileNumber(dto.mobileNumber())
			.message(dto.message())
			.priority(PriorityEnum.HIGH);
	}
}
