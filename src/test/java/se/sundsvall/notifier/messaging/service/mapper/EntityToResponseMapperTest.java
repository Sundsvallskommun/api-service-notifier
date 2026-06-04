package se.sundsvall.notifier.messaging.service.mapper;

import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;
import se.sundsvall.notifier.messaging.api.model.response.EmployeeManagerResponse;
import se.sundsvall.notifier.messaging.api.model.response.EmployeeWithOrgNameResponse;
import se.sundsvall.notifier.messaging.api.model.response.OrganizationResponse;
import se.sundsvall.notifier.messaging.integration.db.entity.Employee;
import se.sundsvall.notifier.messaging.integration.db.entity.Group;
import se.sundsvall.notifier.messaging.integration.db.entity.Organization;

import static org.assertj.core.api.Assertions.assertThat;

public class EntityToResponseMapperTest {

	private final EntityToResponseMapper mapper = new EntityToResponseMapper();

	@Test
	void mapToGroupResponse_withEmployee() {
		var createdAt = LocalDateTime.of(2025, 2, 1, 20, 15);

		var group = Group.builder()
			.withId(1L)
			.withName("testGroup")
			.withDescription("testDescription")
			.withCreatorId("creatorId")
			.withCreatedAt(createdAt)
			.build();

		var org = new Organization();
		org.setName("org1");

		var emp1 = new Employee();
		emp1.setPersonId("p1");
		emp1.setOrgId("org1");
		emp1.setFirstName("firstName1");
		emp1.setLastName("lastName1");
		emp1.setEmail("test1@test.se");
		emp1.setWorkMobile("0701234567");
		emp1.setWorkPhone("0601234567");
		emp1.setWorkTitle("t1");
		emp1.setOrganization(org);

		group.setEmployees(Set.of(emp1));

		var response = mapper.mapToGroupResponse(group);

		assertThat(response.employees()).hasSize(1);
		var mapped = response.employees().iterator().next();
		assertThat(mapped.personId()).isEqualTo("p1");
		assertThat(mapped.orgId()).isEqualTo("org1");
	}

	@Test
	void mapToGroupResponse_withoutEmployees() {
		var group = Group.builder()
			.withId(1L)
			.withName("testGroup")
			.withDescription("testDescription")
			.withCreatorId("creatorId")
			.withCreatedAt(LocalDateTime.of(2025, 2, 1, 20, 37))
			.withEmployees(Set.of())
			.build();

		var response = mapper.mapToGroupResponse(group);

		assertThat(response.employees())
			.isNotNull()
			.isEmpty();
	}

	@Test
	void mapToEmployeeWithOrgName() {
		var organization = new Organization();
		organization.setName("Sundsvalls kommun");

		var employee = new Employee();
		employee.setId(123L);
		employee.setPersonId("p1");
		employee.setOrgId("org1");
		employee.setFirstName("firstTest");
		employee.setLastName("lastTest");
		employee.setEmail("test@test.se");
		employee.setWorkMobile("0701234567");
		employee.setWorkPhone("0601234567");
		employee.setWorkTitle("testPerson");
		employee.setOrganization(organization);

		EmployeeWithOrgNameResponse response = mapper.mapToEmployeeWithOrgNameResponse(employee);

		assertThat(response.id()).isEqualTo(123L);
		assertThat(response.personId()).isEqualTo("p1");
		assertThat(response.orgId()).isEqualTo("org1");
		assertThat(response.firstName()).isEqualTo("firstTest");
		assertThat(response.lastName()).isEqualTo("lastTest");
		assertThat(response.email()).isEqualTo("test@test.se");
		assertThat(response.workMobile()).isEqualTo("0701234567");
		assertThat(response.workPhone()).isEqualTo("0601234567");
		assertThat(response.workTitle()).isEqualTo("testPerson");
		assertThat(response.orgName()).isEqualTo("Sundsvalls kommun");
	}

	@Test
	void mapToOrganizationResponse() {
		var organization = new Organization();
		organization.setCompanyId("556000-0000");
		organization.setParentOrgId("parent-1");
		organization.setOrgId("org-1");
		organization.setName("IT-avdelningen");
		organization.setTreeLevel(3);

		OrganizationResponse response = mapper.mapToOrganizationResponse(organization);

		assertThat(response.companyId()).isEqualTo("556000-0000");
		assertThat(response.parentOrgId()).isEqualTo("parent-1");
		assertThat(response.orgId()).isEqualTo("org-1");
		assertThat(response.name()).isEqualTo("IT-avdelningen");
		assertThat(response.treeLevel()).isEqualTo(3);
	}

	@Test
	void mapToEmployeeManagerResponse() {
		var emp = new Employee();
		emp.setId(123L);
		emp.setPersonId("p1");
		emp.setOrgId("org1");
		emp.setFirstName("firstTest");
		emp.setLastName("lastTest");
		emp.setEmail("test@test.se");
		emp.setWorkMobile("0701234567");
		emp.setWorkPhone("0601234567");
		emp.setWorkTitle("testPerson");
		emp.setManagerCode("managerCode");

		EmployeeManagerResponse response = mapper.mapToEmployeeManagerResponse(emp);

		assertThat(response.managerCode()).isEqualTo("managerCode");
		assertThat(response.id()).isEqualTo(123L);
		assertThat(response.personId()).isEqualTo("p1");
		assertThat(response.orgId()).isEqualTo("org1");
		assertThat(response.firstName()).isEqualTo("firstTest");
		assertThat(response.lastName()).isEqualTo("lastTest");
		assertThat(response.email()).isEqualTo("test@test.se");
		assertThat(response.workMobile()).isEqualTo("0701234567");
		assertThat(response.workPhone()).isEqualTo("0601234567");
		assertThat(response.workTitle()).isEqualTo("testPerson");
	}
}
