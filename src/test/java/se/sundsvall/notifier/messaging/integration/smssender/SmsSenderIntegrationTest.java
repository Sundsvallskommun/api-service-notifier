package se.sundsvall.notifier.messaging.integration.smssender;

import generated.se.sundsvall.smssender.SendSmsRequest;
import generated.se.sundsvall.smssender.SendSmsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmsSenderIntegrationTest {

	@Mock
	private SmsSenderClient client;

	@Mock
	private SmsSenderIntegrationMapper mapper;

	private SmsSenderIntegration integration;

	@BeforeEach
	void setup() {
		integration = new SmsSenderIntegration(client, mapper);
	}

	@Test
	void sendSms_sent() {
		var municipalityId = "2281";
		var dto = mock(SmsDto.class);

		var request = new SendSmsRequest();
		when(mapper.toSendSmsRequest(dto)).thenReturn(request);

		var body = new SendSmsResponse().sent(true);
		when(client.sendSms(municipalityId, request))
			.thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

		var result = integration.sendSms(municipalityId, dto);

		assertThat(result).isEqualTo(MessageStatus.SENT);
		verify(mapper).toSendSmsRequest(dto);
		verify(client).sendSms(municipalityId, request);
		verifyNoMoreInteractions(client, mapper);
	}

	@Test
	void sendSms_bodyNull() {
		var municipalityId = "2281";
		var dto = mock(SmsDto.class);

		when(mapper.toSendSmsRequest(dto)).thenReturn(new SendSmsRequest());
		when(client.sendSms(eq(municipalityId), any(SendSmsRequest.class)))
			.thenReturn(ResponseEntity.ok().build());

		var result = integration.sendSms(municipalityId, dto);

		assertThat(result).isEqualTo(MessageStatus.NOT_SENT);
		verify(mapper).toSendSmsRequest(dto);
		verify(client).sendSms(eq(municipalityId), any(SendSmsRequest.class));
		verifyNoMoreInteractions(client, mapper);
	}

	@Test
	void sendSms_sentFalse() {
		var municipalityId = "2281";
		var dto = mock(SmsDto.class);

		when(mapper.toSendSmsRequest(dto)).thenReturn(new SendSmsRequest());

		var body = new SendSmsResponse().sent(false);
		when(client.sendSms(eq(municipalityId), any(SendSmsRequest.class)))
			.thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

		var result = integration.sendSms(municipalityId, dto);

		assertThat(result).isEqualTo(MessageStatus.NOT_SENT);
		verify(mapper).toSendSmsRequest(dto);
		verify(client).sendSms(eq(municipalityId), any(SendSmsRequest.class));
		verifyNoMoreInteractions(client, mapper);
	}

	@Test
	void sendSms_badRequest() {
		var municipalityId = "2281";
		var dto = mock(SmsDto.class);

		when(mapper.toSendSmsRequest(dto)).thenReturn(new SendSmsRequest());

		var body = new SendSmsResponse().sent(true);
		when(client.sendSms(eq(municipalityId), any(SendSmsRequest.class)))
			.thenReturn(new ResponseEntity<>(body, HttpStatus.BAD_REQUEST));

		var result = integration.sendSms(municipalityId, dto);

		assertThat(result).isEqualTo(MessageStatus.NOT_SENT);
		verify(mapper).toSendSmsRequest(dto);
		verify(client).sendSms(eq(municipalityId), any(SendSmsRequest.class));
		verifyNoMoreInteractions(client, mapper);
	}

	@Test
	void sendSms_exception() {
		var municipalityId = "2281";
		var dto = mock(SmsDto.class);

		when(client.sendSms(eq(municipalityId), any()))
			.thenThrow(new RuntimeException("Connection refused"));
		var result = integration.sendSms(municipalityId, dto);

		assertThat(result).isEqualTo(MessageStatus.NOT_SENT);
	}
}
