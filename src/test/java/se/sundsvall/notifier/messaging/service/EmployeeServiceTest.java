package se.sundsvall.notifier.messaging.service;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import se.sundsvall.notifier.messaging.api.model.response.EmployeeManagerResponse;
import se.sundsvall.notifier.messaging.api.model.response.EmployeeWithOrgNameResponse;
import se.sundsvall.notifier.messaging.integration.db.entity.Employee;
import se.sundsvall.notifier.messaging.integration.db.repository.EmployeeRepository;
import se.sundsvall.notifier.messaging.service.mapper.EntityToResponseMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

	@Mock
	private EntityToResponseMapper mapper;

	@Mock
	private EmployeeRepository employeeRepository;

	@Test
	void byOrg_test() {
		var service = new EmployeeService(employeeRepository, mapper);

		var employee1 = new Employee();
		var employee2 = new Employee();
		var response1 = mock(EmployeeWithOrgNameResponse.class);
		var response2 = mock(EmployeeWithOrgNameResponse.class);

		when(employeeRepository.findByOrgIdAndActiveEmployeeTrue("Id")).thenReturn(List.of(employee1, employee2));
		when(mapper.mapToEmployeeWithOrgNameResponse(employee1)).thenReturn(response1);
		when(mapper.mapToEmployeeWithOrgNameResponse(employee2)).thenReturn(response2);

		var result = service.getEmployeesByOrg("Id");

		assertThat(List.of(response1, response2)).isEqualTo(result);
	}

	@Test
	void byOrgList_test() {
		var service = new EmployeeService(employeeRepository, mapper);

		var employee1 = new Employee();
		var employee2 = new Employee();
		var response1 = mock(EmployeeWithOrgNameResponse.class);
		var response2 = mock(EmployeeWithOrgNameResponse.class);

		when(employeeRepository.findByOrgIdInAndActiveEmployeeTrue(List.of("Id1", "Id2"))).thenReturn(List.of(employee1, employee2));
		when(mapper.mapToEmployeeWithOrgNameResponse(employee1)).thenReturn(response1);
		when(mapper.mapToEmployeeWithOrgNameResponse(employee2)).thenReturn(response2);

		var result = service.getEmployeesByOrgList(List.of("Id1", "Id2"));

		assertThat(List.of(response1, response2)).isEqualTo(result);
	}

	@Test
	void getAll_test() {
		var service = new EmployeeService(employeeRepository, mapper);

		var employee1 = new Employee();
		var employee2 = new Employee();
		var response1 = mock(EmployeeWithOrgNameResponse.class);
		var response2 = mock(EmployeeWithOrgNameResponse.class);

		when(employeeRepository.findByActiveEmployeeTrue()).thenReturn(List.of(employee1, employee2));
		when(mapper.mapToEmployeeWithOrgNameResponse(employee1)).thenReturn(response1);
		when(mapper.mapToEmployeeWithOrgNameResponse(employee2)).thenReturn(response2);

		var result = service.getAllEmployees();

		assertThat(List.of(response1, response2)).isEqualTo(result);
	}

	@Test
	void getEmployee_Org_Id_Null_test() {
		var service = new EmployeeService(employeeRepository, mapper);

		var exception = assertThrows(IllegalArgumentException.class, () -> service.getEmployeesByOrgList(null));

		assertThat(exception.getMessage()).isEqualTo("org id is required");
		verifyNoInteractions(employeeRepository, mapper);
	}

	@Test
	void getWithPartialSearch_test() {
		var service = new EmployeeService(employeeRepository, mapper);

		var employee1 = new Employee();
		var employee2 = new Employee();
		var response1 = mock(EmployeeWithOrgNameResponse.class);
		var response2 = mock(EmployeeWithOrgNameResponse.class);

		Pageable page = PageRequest.of(0, 2,
			Sort.by(Sort.Order.asc("firstName"), Sort.Order.asc("lastName")));
		Pageable sortedPage = PageRequest.of(0, 2, Sort.by("firstName").ascending().and(Sort.by("lastName").ascending()));

		Page<Employee> employeePage = new PageImpl<>(List.of(employee1, employee2), sortedPage, 2);

		when(employeeRepository.findMatchingEmployee("searchterm1", "serachterm2", sortedPage)).thenReturn(employeePage);
		when(mapper.mapToEmployeeWithOrgNameResponse(employee1)).thenReturn(response1);
		when(mapper.mapToEmployeeWithOrgNameResponse(employee2)).thenReturn(response2);

		var result = service.getEmployeesWithSearch("searchterm1 serachterm2", page);

		assertThat(result.getContent()).containsExactly(response1, response2);
	}

	@Test
	void getEmployee_withManagerCode_test() {
		var service = new EmployeeService(employeeRepository, mapper);

		var employee1 = new Employee();
		var employee2 = new Employee();

		var response1 = EmployeeManagerResponse.builder()
			.withId(1L)
			.withPersonId("p1")
			.withOrgId("org1")
			.withFirstName("Anna")
			.withLastName("Andersson")
			.withEmail("anna@example.com")
			.withWorkMobile("0700000001")
			.withWorkPhone("060000001")
			.withWorkTitle("Chef")
			.withManagerCode("MGR1")
			.build();

		var response2 = EmployeeManagerResponse.builder()
			.withId(2L)
			.withPersonId("p2")
			.withOrgId("org2")
			.withFirstName("Bertil")
			.withLastName("Berg")
			.withEmail("bertil@example.com")
			.withWorkMobile("0700000002")
			.withWorkPhone("060000002")
			.withWorkTitle("Team lead")
			.withManagerCode("MGR2")
			.build();

		when(employeeRepository.findAllByManagerCodeIsNotNullAndActiveEmployeeTrue()).thenReturn(List.of(employee1, employee2));
		when(mapper.mapToEmployeeManagerResponse(employee1)).thenReturn(response1);
		when(mapper.mapToEmployeeManagerResponse(employee2)).thenReturn(response2);

		var result = service.getAllEmployeeManagers();

		assertThat(result).isEqualTo(List.of(response1, response2));
	}

	@Test
	void getEmployee_withManagerCode_emptyList_test() {
		var service = new EmployeeService(employeeRepository, mapper);

		when(employeeRepository.findAllByManagerCodeIsNotNullAndActiveEmployeeTrue()).thenReturn(List.of());

		var result = service.getAllEmployeeManagers();

		assertThat(result).isEmpty();
		verifyNoInteractions(mapper);
	}
}
