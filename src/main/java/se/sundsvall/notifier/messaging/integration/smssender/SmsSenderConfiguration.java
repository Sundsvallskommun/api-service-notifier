package se.sundsvall.notifier.messaging.integration.smssender;

import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

public class SmsSenderConfiguration {

	public static final String CLIENT_ID = "sms-sender";

	// sms-sender is an internal, unauthenticated service on the docker network — no outbound OAuth2.
	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(final SmsSenderProperties properties) {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(new ProblemErrorDecoder(CLIENT_ID))
			.withRequestTimeoutsInSeconds(properties.connectTimeout(), properties.readTimeout())
			.composeCustomizersToOne();
	}
}
