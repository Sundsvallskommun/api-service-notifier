package apptest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.notifier.Application;

@WireMockAppTestSuite(files = "classpath:/LoginIT/", classes = Application.class)
@Sql(scripts = {
        "/db/script/truncate.sql",
        "/db/script/GetUserTest.sql"
})
class LoginIT extends AbstractAppTest {

    @Test
    void test01_loginSuccess() {
        setupCall()
                .withServicePath("/api/users/auth/login")
                .withHttpMethod(HttpMethod.POST)
                .withRequest("request.json")
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse();
    }
    @Test
    void test02_loginFailed() {
        setupCall()
                .withServicePath("/api/users/auth/login")
                .withHttpMethod(HttpMethod.POST)
                .withRequest("request.json")
                .withExpectedResponseStatus(HttpStatus.UNAUTHORIZED)
                .sendRequestAndVerifyResponse();
    }

    @Test
    void test03_logoutSuccess() {
        setupCall()
                .withServicePath("/api/users/auth/logout")
                .withHttpMethod(HttpMethod.POST)
                .withExpectedResponseStatus(HttpStatus.OK)
                .sendRequestAndVerifyResponse();
    }
}
