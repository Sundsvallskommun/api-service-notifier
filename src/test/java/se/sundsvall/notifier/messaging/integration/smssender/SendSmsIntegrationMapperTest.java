package se.sundsvall.notifier.messaging.integration.smssender;

import generated.se.sundsvall.smssender.SendSmsRequest.PriorityEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SendSmsIntegrationMapperTest {
	private SmsSenderIntegrationMapper mapper;

	@BeforeEach
	void setup() {
		mapper = new SmsSenderIntegrationMapper();
		ReflectionTestUtils.setField(mapper, "sender", "Sundsvall");
	}

	@Test
	void toSendSmsRequest_dtoOk() {
		var dto = SmsDto.builder()
			.withSender("ShouldBeIgnored")
			.withMobileNumber("0701234567")
			.withMessage("testMessage")
			.withPriority(null) // is ignored, we set priority to high in the mapper
			.build();

		var result = mapper.toSendSmsRequest(dto);

		assertNotNull(result.getSender());
		assertEquals("Sundsvall", result.getSender().getName());

		assertEquals("0701234567", result.getMobileNumber());
		assertEquals("testMessage", result.getMessage());

		assertEquals(PriorityEnum.HIGH, result.getPriority());

	}

	@Test
	void toSendSmsRequest_dtoNull() {
		assertNull(mapper.toSendSmsRequest(null));
	}
}
