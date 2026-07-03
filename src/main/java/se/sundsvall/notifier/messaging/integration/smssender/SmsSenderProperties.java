package se.sundsvall.notifier.messaging.integration.smssender;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("integration.sms-sender")
public record SmsSenderProperties(
	@DefaultValue("5") int readTimeout,
	@DefaultValue("30") int connectTimeout) {
}
