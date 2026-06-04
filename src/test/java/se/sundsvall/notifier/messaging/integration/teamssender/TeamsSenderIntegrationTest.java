package se.sundsvall.notifier.messaging.integration.teamssender;

import generated.se.sundsvall.teamssender.SendTeamsMessageRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.Problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(MockitoExtension.class)
class TeamsSenderIntegrationTest {

	@Mock
	private TeamsSenderIntegrationMapper mapper;
	@Mock
	private TeamsSenderClient teamsSenderClient;

	@InjectMocks
	private TeamsSenderIntegration teamsSenderIntegration;

	@Test
	void sendTeamsMessageTest() {
		final var message = "message";
		final var recipient = "test@test.se";
		final var municipalityId = "2281";
		final var dto = TeamsSenderDTO.builder()
			.withMessage(message)
			.withRecipient(recipient)
			.build();

		final var request = new SendTeamsMessageRequest();
		request.setMessage(message);
		request.setRecipient(recipient);

		when(mapper.toSendTeamsMessageRequest(dto)).thenReturn(request);

		var result = teamsSenderIntegration.sendTeamsMessage(municipalityId, dto);

		assertThat(result).isTrue();
		verify(mapper).toSendTeamsMessageRequest(dto);
		verify(teamsSenderClient).sendTeamsMessage(municipalityId, request);
		verifyNoMoreInteractions(teamsSenderClient, mapper);
	}

	@Test
	void sendTeamsMessageFailureTest() {
		final var request = new SendTeamsMessageRequest();

		when(mapper.toSendTeamsMessageRequest(any(TeamsSenderDTO.class))).thenReturn(request);
		when(teamsSenderClient.sendTeamsMessage(anyString(), any(SendTeamsMessageRequest.class))).thenThrow(Problem.builder()
			.withStatus(BAD_GATEWAY)
			.withCause(Problem.builder()
				.withStatus(BAD_REQUEST)
				.build())
			.build());
		var result = teamsSenderIntegration.sendTeamsMessage(null, null);

		assertThat(result).isFalse();
	}
}
