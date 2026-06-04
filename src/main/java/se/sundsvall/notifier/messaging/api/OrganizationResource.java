package se.sundsvall.notifier.messaging.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import se.sundsvall.notifier.messaging.api.model.response.OrganizationResponse;
import se.sundsvall.notifier.messaging.service.OrganizationService;

@RestController
@RequestMapping(path = "/api/notifier/organization", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Organization Resource")
@ApiResponses({
	@ApiResponse(
		responseCode = "200",
		description = "Successful Operation",
		useReturnTypeSchema = true),
	@ApiResponse(
		responseCode = "500",
		description = "Internal Server Error",
		content = @Content(schema = @Schema(implementation = Problem.class))

	)

})
public class OrganizationResource {

	private final OrganizationService organizationService;

	public OrganizationResource(OrganizationService organizationService) {
		this.organizationService = organizationService;
	}

	@Operation(summary = "Get a specific organization",
		responses = @ApiResponse(
			responseCode = "404",
			description = "Not Found",
			content = @Content(schema = @Schema(implementation = Problem.class))))
	@GetMapping("/{orgId}")
	public ResponseEntity<OrganizationResponse> getSpecificOrganization(@PathVariable String orgId) {
		return ResponseEntity.ok(organizationService.getSpecificOrg(orgId));
	}

	@Operation(summary = "Get all organzations")
	@GetMapping("/organizations")
	public ResponseEntity<List<OrganizationResponse>> getAllOrganizations() {
		return ResponseEntity.ok(organizationService.getAllOrganizations());
	}

	@Operation(summary = "Get multiple organizations with a list of organization ids", responses = {
		@ApiResponse(
			responseCode = "404",
			description = "Not Found",
			content = @Content(schema = @Schema(implementation = Problem.class))),
	})
	@GetMapping("/ids")
	public ResponseEntity<List<OrganizationResponse>> getOrganizationsWithList(@RequestParam List<String> orgId) {
		return ResponseEntity.ok(organizationService.getOrgsById(orgId));
	}

	@Operation(summary = "Get an organization and all organizations under it", responses = {
		@ApiResponse(
			responseCode = "404",
			description = "Not Found",
			content = @Content(schema = @Schema(implementation = Problem.class))),
	})
	@GetMapping("/{orgId}/children/descendants")
	public ResponseEntity<List<OrganizationResponse>> getOrganizationWithChildrenAndDescendants(@PathVariable String orgId) {
		return ResponseEntity.ok(organizationService.getOrgChildrenAndDescendantsWithId(orgId));
	}

	@Operation(summary = "Get an organizations children", responses = {
		@ApiResponse(
			responseCode = "404",
			description = "Not Found",
			content = @Content(schema = @Schema(implementation = Problem.class))),
	})
	@GetMapping("/{orgId}/children")
	public ResponseEntity<List<OrganizationResponse>> getChildren(@PathVariable String orgId) {
		return ResponseEntity.ok(organizationService.getChildrenReplaceDuplicateDescendantsWithRoot(orgId));
	}

	@Operation(summary = "Searches for organization matching search term")
	@GetMapping("/organizations/search")
	public ResponseEntity<Page<OrganizationResponse>> getOrganizationPartialSearch(@RequestParam String search, Pageable pageable) {
		return ResponseEntity.ok(organizationService.getOrganizationWithSearch(search, pageable));
	}

}
