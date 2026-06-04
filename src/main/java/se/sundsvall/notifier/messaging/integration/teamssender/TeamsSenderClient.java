package se.sundsvall.notifier.messaging.integration.teamssender;

import generated.se.sundsvall.teamssender.SendTeamsMessageRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static se.sundsvall.notifier.messaging.integration.teamssender.TeamsSenderConfiguration.CLIENT_ID;

@FeignClient(
	name = CLIENT_ID,
	url = "${integration.teams-sender.base-url}",
	configuration = TeamsSenderConfiguration.class)

@CircuitBreaker(name = CLIENT_ID)
public interface TeamsSenderClient {
	@PostMapping("/{municipalityId}/teams/messages")
	ResponseEntity<Void> sendTeamsMessage(@PathVariable String municipalityId, @RequestBody SendTeamsMessageRequest request);
}
