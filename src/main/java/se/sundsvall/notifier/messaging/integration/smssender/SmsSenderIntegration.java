package se.sundsvall.notifier.messaging.integration.smssender;

import generated.se.sundsvall.smssender.SendSmsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;
import static se.sundsvall.notifier.messaging.integration.smssender.MessageStatus.NOT_SENT;
import static se.sundsvall.notifier.messaging.integration.smssender.MessageStatus.SENT;

@Slf4j
@Component
public class SmsSenderIntegration {

	private final SmsSenderClient client;
	private final SmsSenderIntegrationMapper mapper;

	SmsSenderIntegration(final SmsSenderClient client, final SmsSenderIntegrationMapper mapper) {
		this.client = client;
		this.mapper = mapper;
	}

	public MessageStatus sendSms(final String municipalityId, final SmsDto dto) {
		try {
			var response = client.sendSms(municipalityId, mapper.toSendSmsRequest(dto));
			var success = response.getStatusCode().is2xxSuccessful() &&
				ofNullable(response.getBody())
					.map(SendSmsResponse::getSent)
					.orElse(false);
			return success ? SENT : NOT_SENT;
		} catch (Exception e) {
			log.error("Failed to send SMS to municipalityId: {}, mobileNumber: {}, sender: {}",
				municipalityId, dto.mobileNumber(), dto.sender(), e);
			return NOT_SENT;
		}
	}
}
