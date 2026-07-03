package se.sundsvall.notifier.messaging.integration.teamssender;

import generated.se.sundsvall.teamssender.SendTeamsMessageRequest;
import org.springframework.stereotype.Component;

@Component
public class TeamsSenderIntegrationMapper {

	public SendTeamsMessageRequest toSendTeamsMessageRequest(final TeamsSenderDTO dto) {
		if (dto == null) {
			return null;
		}

		return new SendTeamsMessageRequest()
			.recipient(dto.recipient())
			.message(dto.message());

	}
}
