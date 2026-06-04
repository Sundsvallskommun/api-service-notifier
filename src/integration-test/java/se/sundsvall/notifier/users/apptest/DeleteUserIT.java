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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Update user apptests.
 *
 * @see /src/test/resources/db/script/UpdateUserAppTest.sql for data setup.
 */
@WireMockAppTestSuite(files = "classpath:/DeleteUserIT/", classes = Application.class)
@Sql(scripts = {
        "/db/script/truncate.sql",
        "/db/script/UpdateUserAppTest.sql"
})
class DeleteUserIT extends AbstractAppTest {
    private static final String REQUEST = "request.json";
    private static final String RESPONSE = "response.json";

    @Autowired
    private UserRepository userRepository;

    @Test
    void test01_deleteUserByEmail() {

        final String email = "testmail1@sundsvall.se";

        assertThat(userRepository.findByEmail(email)).isPresent();

        setupCall()
                .withServicePath("/api/users/emails/".concat(email))
                .withHttpMethod(HttpMethod.DELETE)
                .withExpectedResponseStatus(HttpStatus.NO_CONTENT)
                .sendRequestAndVerifyResponse();

        assertThat(userRepository.findByEmail(email)).isEmpty();

    }

    @Test
    void test02_deleteUserId() {

        final Long id = 1L;

        assertThat(userRepository.findById(id)).isPresent();

        setupCall()
                .withServicePath("/api/users/ids/" + id)
                .withHttpMethod(HttpMethod.DELETE)
                .withExpectedResponseStatus(HttpStatus.NO_CONTENT)
                .sendRequestAndVerifyResponse();

        assertThat(userRepository.findById(id)).isEmpty();


    }
}
