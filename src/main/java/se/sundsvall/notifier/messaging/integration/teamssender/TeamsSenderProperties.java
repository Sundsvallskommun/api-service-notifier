package se.sundsvall.notifier.messaging.integration.teamssender;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("integration.teams-sender")
public record TeamsSenderProperties(
	@DefaultValue("5") int readTimeout,
	@DefaultValue("30") int connectTimeout) {
}
