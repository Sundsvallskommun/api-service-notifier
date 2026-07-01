package apptest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.notifier.Application;
import se.sundsvall.notifier.users.integration.db.UserRepository;
import se.sundsvall.notifier.users.integration.db.model.enums.Status;


import static org.assertj.core.api.Assertions.assertThat;


@WireMockAppTestSuite(files = "classpath:/CreateUsersIT/", classes = Application.class)
class CreateUsersIT extends AbstractAppTest {
    private static final String REQUEST = "request.json";

    @Autowired
    private UserRepository userRepository;

    @Test
    void test01_createUser() {
        assertThat(userRepository.findByEmail("test1@sundsvall.se")).isEmpty();

        setupCall()
                .withServicePath("/api/users")
                .withHttpMethod(HttpMethod.POST)
                .withRequest(REQUEST)
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .sendRequestAndVerifyResponse();

        final var user = userRepository.findByEmail("test@sundsvall.se");
        assertThat(user).isPresent();
        assertThat(user.get().getStatus()).isNotNull();
        assertThat(user.get().getEmail()).isEqualTo("test@sundsvall.se");
        assertThat(user.get().getPhoneNumber()).isEqualTo("0701234567");
        assertThat(user.get().getMunicipalityId()).isEqualTo("2281");
        assertThat(user.get().getStatus()).isEqualTo(Status.INACTIVE);
    }
}
