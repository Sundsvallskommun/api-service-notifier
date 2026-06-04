package se.sundsvall.notifier.messaging.integration.teamssender;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TeamsSenderIntegrationMapperTest {

	private final TeamsSenderIntegrationMapper mapper = new TeamsSenderIntegrationMapper();

	@Test
	void toSendTeamsMessageRequestTest() {

		var dto = TeamsSenderDTO.builder()
			.withMessage("message")
			.withRecipient("recipient")
			.build();
		var result = mapper.toSendTeamsMessageRequest(dto);

		assertThat(result).isNotNull();
		assertThat(result.getMessage()).isEqualTo("message");
		assertThat(result.getRecipient()).isEqualTo("recipient");

	}

	@Test
	void toSendTeamsMessageRequestIsNullTest() {
		assertThat(mapper.toSendTeamsMessageRequest(null)).isNull();
	}

}
