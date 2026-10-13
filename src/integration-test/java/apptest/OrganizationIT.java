package apptest;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.notifier.Application;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@WireMockAppTestSuite(files = "classpath:/OrganizationIT/", classes = Application.class)
@Testcontainers
@Sql(scripts = {
	"/db/script/truncate.sql",
	"/db/script/testdata.sql"
})
public class OrganizationIT extends AbstractAppTest {

	private static final String PATH = "/api/notifier/organization";
	private static final String EXPECTED_FILE = "expected.json";

	@Test
	void test1_getSpecificOrganizationSuccess() {
		setupCall()
			.withServicePath(PATH + "/ORG-1")
			.withHttpMethod(GET)
			.withExpectedResponse(EXPECTED_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_getSpecificOrganizationNotFound() {
		setupCall()
			.withServicePath(PATH + "/ORG-99")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_getAllOrganizationsSuccess() {
		setupCall()
			.withServicePath(PATH + "/organizations")
			.withHttpMethod(GET)
			.withExpectedResponse(EXPECTED_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_getOrganizationsWithListSuccess() {
		setupCall()
			.withServicePath(PATH + "/ids?orgId=ORG-1,ORG-2")
			.withHttpMethod(GET)
			.withExpectedResponse(EXPECTED_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test5_getOrganizationsWithListNotFound() {
		setupCall()
			.withServicePath(PATH + "/ids?orgId=ORG-121,ORG-29")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test6_getOrganizationWithChildrenAndDescendantsSuccess() {
		setupCall()
			.withServicePath(PATH + "/ORG-1/children/descendants")
			.withHttpMethod(GET)
			.withExpectedResponse(EXPECTED_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test7_getOrganizationWithChildrenAndDescendantsNotFound() {
		setupCall()
			.withServicePath(PATH + "/ORG-999/children/descendants")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test8_getChildrenSuccess() {
		setupCall()
			.withServicePath(PATH + "/ORG-1/children")
			.withHttpMethod(GET)
			.withExpectedResponse(EXPECTED_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test9_getChildrenNotFound() {
		setupCall()
			.withServicePath(PATH + "/ORG-999/children")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test10_getOrganizationPartialSearchSuccess() {
		setupCall()
			.withServicePath(PATH + "/organizations/search?search=org&page=0&size=1")
			.withHttpMethod(GET)
			.withExpectedResponse(EXPECTED_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test11_getOrganizationPartialSearchEmptyPage() {
		setupCall()
			.withServicePath(PATH + "/organizations/search?search=test&page=0&size=1")
			.withHttpMethod(GET)
			.withExpectedResponse(EXPECTED_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}
}
