package se.sundsvall.notifier.messaging.integration.teamssender;

import org.springframework.stereotype.Component;

@Component
public class TeamsSenderIntegration {

	private final TeamsSenderClient teamsSenderClient;
	private final TeamsSenderIntegrationMapper mapper;

	public TeamsSenderIntegration(TeamsSenderClient teamsSenderClient, TeamsSenderIntegrationMapper mapper) {
		this.teamsSenderClient = teamsSenderClient;
		this.mapper = mapper;
	}

	public Boolean sendTeamsMessage(final String municipalityId, final TeamsSenderDTO dto) {
		try {
			teamsSenderClient.sendTeamsMessage(municipalityId, mapper.toSendTeamsMessageRequest(dto));
			return true;
		} catch (Exception e) {
			return false;
		}

	}
}
