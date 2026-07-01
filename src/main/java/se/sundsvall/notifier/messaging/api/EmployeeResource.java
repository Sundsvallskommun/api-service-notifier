package se.sundsvall.notifier.messaging.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.notifier.messaging.api.model.response.EmployeeManagerResponse;
import se.sundsvall.notifier.messaging.api.model.response.EmployeeWithOrgNameResponse;
import se.sundsvall.notifier.messaging.service.EmployeeService;

@RestController
@RequestMapping(path = "/api/notifier/employees", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Employee Resource")
@ApiResponse(
	responseCode = "200",
	description = "Successful Operation",
	useReturnTypeSchema = true)
@ApiResponse(
	responseCode = "500",
	description = "Internal Server Error",
	content = @Content(schema = @Schema(implementation = Problem.class)))

public class EmployeeResource {

	private final EmployeeService employeeService;

	public EmployeeResource(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	@Operation(summary = "Get employee data from specific organization")
	@GetMapping("/{orgId}")
	public ResponseEntity<List<EmployeeWithOrgNameResponse>> getEmployeesByOrgId(@PathVariable String orgId) {
		return ResponseEntity.ok(employeeService.getEmployeesByOrg(orgId));
	}

	@Operation(summary = "Get employee data from all organizations")
	@GetMapping
	public ResponseEntity<List<EmployeeWithOrgNameResponse>> getAllEmployees() {
		return ResponseEntity.ok(employeeService.getAllEmployees());
	}

	@GetMapping("/orgIds")
	public ResponseEntity<List<EmployeeWithOrgNameResponse>> getAllEmployeesWithOrgIdList(@RequestParam List<String> orgIds) {
		return ResponseEntity.ok(employeeService.getEmployeesByOrgList(orgIds));
	}

	@Operation(summary = "Searches for employees matching search term")
	@GetMapping("/search")
	public ResponseEntity<Page<EmployeeWithOrgNameResponse>> getEmployeesPartialSearch(@RequestParam String search, Pageable pageable) {
		return ResponseEntity.ok(employeeService.getEmployeesWithSearch(search, pageable));
	}

	@Operation(summary = "Get all employees with manager-code")
	@GetMapping("/managers")
	public ResponseEntity<List<EmployeeManagerResponse>> getManagers() {
		return ResponseEntity.ok(employeeService.getAllEmployeeManagers());
	}

	@Operation(summary = "Get all employees for it-prod")
	@GetMapping("/org-group/it-prod")
	public ResponseEntity<List<EmployeeWithOrgNameResponse>> getEmployeesForItProd() {
		return ResponseEntity.ok(employeeService.getAllEmployeesForItProd());
	}
}
