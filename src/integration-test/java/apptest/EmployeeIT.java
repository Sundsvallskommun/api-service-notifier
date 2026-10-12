package apptest;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.notifier.Application;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@WireMockAppTestSuite(files = "classpath:/EmployeeIT/", classes = Application.class)
@Testcontainers
@Sql(scripts = {
	"/db/script/truncate.sql",
	"/db/script/testdata.sql"
})
public class EmployeeIT extends AbstractAppTest {
	private static final String PATH = "/api/notifier/employees";
	private static final String RESPONSE_FILE = "response.json";

	@Test
	void test1_getEmployeesByOrgIdSuccess() {
		setupCall()
			.withServicePath(PATH + "/ORG-1")
			.withHttpMethod(GET)
			.withExpectedResponse(RESPONSE_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_getAllEmployeesSuccess() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(GET)
			.withExpectedResponse(RESPONSE_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_getAllEmployeesWithOrgIdListSuccess() {
		setupCall()
			.withServicePath(PATH + "/orgIds?orgIds=ORG-1,ORG-2")
			.withHttpMethod(GET)
			.withExpectedResponse(RESPONSE_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_getEmployeesPartialSearch() {
		setupCall()
			.withServicePath(PATH + "/search?search=one&page=0&size=1")
			.withHttpMethod(GET)
			.withExpectedResponse(RESPONSE_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test5_getManagersSuccess() {
		setupCall()
			.withServicePath(PATH + "/managers")
			.withHttpMethod(GET)
			.withExpectedResponse(RESPONSE_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test6_getEmployeesByOrgId_emptyList() {
		setupCall()
			.withServicePath(PATH + "/ORG-99")
			.withHttpMethod(GET)
			.withExpectedResponse(RESPONSE_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test7_searchEmployees_noResult() {
		setupCall()
			.withServicePath(PATH + "/search?search=abcdefgh&page=0&size=1")
			.withHttpMethod(GET)
			.withExpectedResponse(RESPONSE_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test8_getEmployeesByOrgList_emptyParam() {
		setupCall()
			.withServicePath(PATH + "/orgIds?orgIds=")
			.withHttpMethod(GET)
			.withExpectedResponse(RESPONSE_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}
}
