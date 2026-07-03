package se.sundsvall.notifier.messaging.integration.smssender;

import generated.se.sundsvall.smssender.SendSmsRequest.PriorityEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

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

		assertThat(result.getSender()).isNotNull();
		assertThat(result.getSender().getName()).isEqualTo("Sundsvall");

		assertThat(result.getMobileNumber()).isEqualTo("0701234567");
		assertThat(result.getMessage()).isEqualTo("testMessage");

		assertThat(result.getPriority()).isEqualTo(PriorityEnum.HIGH);

	}

	@Test
	void toSendSmsRequest_dtoNull() {
		assertThat(mapper.toSendSmsRequest(null)).isNull();
	}
}
