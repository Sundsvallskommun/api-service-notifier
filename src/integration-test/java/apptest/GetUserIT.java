package apptest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.notifier.Application;
import se.sundsvall.notifier.users.integration.db.UserRepository;

@WireMockAppTestSuite(files = "classpath:/GetUserIT/", classes = Application.class)
@Sql(scripts = {
        "/db/script/truncate.sql",
        "/db/script/GetUserTest.sql"
})
class GetUserIT extends AbstractAppTest {
    private static final String RESPONSE = "response.json";
    @Autowired
    UserRepository userRepository;

    @Test
    void test01_getUserByEmail() {
        final var email = "testmail1@sundsvall.se";
		
        setupCall()
                .withServicePath("/api/users/emails/".concat(email))
                .withHttpMethod(HttpMethod.GET)
                .withExpectedResponseStatus(HttpStatus.OK)
                .withExpectedResponse(RESPONSE)
                .sendRequestAndVerifyResponse();
    }

    @Test
    void test02_getUserById() {
        final Long id = 1L;
		
        setupCall()
                .withServicePath("/api/users/ids/" + id)
                .withHttpMethod(HttpMethod.GET)
                .withExpectedResponseStatus(HttpStatus.OK)
                .withExpectedResponse(RESPONSE)
                .sendRequestAndVerifyResponse();
    }
}