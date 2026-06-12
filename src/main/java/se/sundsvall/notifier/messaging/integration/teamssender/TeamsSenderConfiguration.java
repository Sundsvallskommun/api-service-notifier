package se.sundsvall.notifier.messaging.integration.teamssender;

import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

public class TeamsSenderConfiguration {

	public static final String CLIENT_ID = "teams-sender";

	// teams-sender is an internal, unauthenticated service on the docker network — no outbound OAuth2.
	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(final TeamsSenderProperties properties) {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(new ProblemErrorDecoder(CLIENT_ID))
			.withRequestTimeoutsInSeconds(properties.connectTimeout(), properties.readTimeout())
			.composeCustomizersToOne();
	}
}
