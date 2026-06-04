package se.sundsvall.notifier.users.apptest;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.notifier.Application;
import se.sundsvall.notifier.users.integration.db.UserRepository;
import se.sundsvall.notifier.users.integration.db.model.enums.Status;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Update user apptests.
 *
 * @see /src/test/resources/db/script/UpdateUserAppTest.sql for data setup.
 */
@WireMockAppTestSuite(files = "classpath:/UpdateUserIT/", classes = Application.class)
@Sql(scripts = {
        "/db/script/truncate.sql",
        "/db/script/UpdateUserAppTest.sql"
})
class UpdateUserIT extends AbstractAppTest {
    private static final String REQUEST = "request.json";
    private static final String RESPONSE = "response.json";

    @Autowired
    private UserRepository userRepository;

    @Test
    void test01_updateUserWithId() {
        final Long id = 1L;

        assertThat(userRepository.findById(id)).isPresent();

        setupCall()
                .withServicePath("/api/users/ids/" + id)
                .withHttpMethod(HttpMethod.PATCH)
                .withRequest(REQUEST)
                .withExpectedResponseStatus(HttpStatus.CREATED)
                .withExpectedResponse(RESPONSE)
                .sendRequestAndVerifyResponse();

        final var user = userRepository.findById(id);

        assertThat(user).isPresent();
        assertThat(user.get().getEmail()).isEqualTo("testmail1@sundsvall.se");
        assertThat(user.get().getId()).isEqualTo(id);
        assertThat(user.get().getPhoneNumber()).isEqualTo("0701234567");
        assertThat(user.get().getMunicipalityId()).isEqualTo("2281");
        assertThat(user.get().getStatus()).isEqualTo(Status.INACTIVE);
    }

    @Test
    void test02_updateUserWithIdNotFound() {
        setupCall()
                .withServicePath("/api/users/ids/999")
                .withHttpMethod(HttpMethod.PATCH)
                .withRequest(REQUEST)
                .withExpectedResponseStatus(HttpStatus.NOT_FOUND)
                .sendRequestAndVerifyResponse();
    }
}
