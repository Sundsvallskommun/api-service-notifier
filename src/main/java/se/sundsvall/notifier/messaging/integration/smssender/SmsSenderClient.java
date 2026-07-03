package se.sundsvall.notifier.messaging.integration.smssender;

import generated.se.sundsvall.smssender.SendSmsRequest;
import generated.se.sundsvall.smssender.SendSmsResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static se.sundsvall.notifier.messaging.integration.smssender.SmsSenderConfiguration.CLIENT_ID;

@FeignClient(
	name = CLIENT_ID,
	url = "${integration.sms-sender.base-url}",
	configuration = SmsSenderConfiguration.class)

@CircuitBreaker(name = CLIENT_ID)
interface SmsSenderClient {
	@PostMapping("/{municipalityId}/send/sms")
	ResponseEntity<SendSmsResponse> sendSms(@PathVariable String municipalityId, @RequestBody SendSmsRequest request);
}
