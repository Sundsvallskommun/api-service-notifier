package se.sundsvall.notifier.messaging.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.notifier.messaging.api.model.request.GroupRequest;
import se.sundsvall.notifier.messaging.api.model.request.GroupUpdateRequest;
import se.sundsvall.notifier.messaging.api.model.response.GroupResponse;
import se.sundsvall.notifier.messaging.service.GroupService;

@RestController
@RequestMapping(path = "/api/notifier/groups", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Group Resource")
@ApiResponse(
	responseCode = "500",
	description = "Internal server error",
	content = @Content(schema = @Schema(implementation = Problem.class)))
public class GroupResource {
	private final GroupService groupService;

	public GroupResource(GroupService groupService) {
		this.groupService = groupService;
	}

	@Operation(summary = "Get all groups or groups created by a specific user", responses = {
		@ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)
	})
	@GetMapping
	public ResponseEntity<List<GroupResponse>> getGroups(@RequestParam(required = false) String creatorId) {
		var responses = (creatorId == null)
			? groupService.getAllGroups()
			: groupService.getGroupsByCreatorId(creatorId);

		return ResponseEntity.ok(responses);
	}

	@Operation(summary = "Get specific group by id", responses = {
		@ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = Problem.class)))
	})
	@GetMapping("/{groupId}")
	public ResponseEntity<GroupResponse> getGroupById(@PathVariable Long groupId) {
		var response = groupService.getGroupById(groupId);

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Create a new group", responses = {
		@ApiResponse(responseCode = "201", description = "Created", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = Problem.class)))
	})
	@PostMapping
	public ResponseEntity<Void> createGroup(@Valid @RequestBody GroupRequest groupRequest) {
		var id = groupService.createGroup(groupRequest);

		return ResponseEntity.created(UriComponentsBuilder.fromPath("/api/notifier/groups/{id}")
			.buildAndExpand(id)
			.toUri())
			.build();
	}

	@Operation(summary = "Update a group", responses = {
		@ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = Problem.class)))
	})
	@PutMapping("/{groupId}")
	public ResponseEntity<GroupResponse> updateGroup(@PathVariable Long groupId, @Valid @RequestBody GroupUpdateRequest groupUpdateRequest) {
		var updatedGroup = groupService.updateGroup(groupId, groupUpdateRequest);

		return ResponseEntity.ok(updatedGroup);
	}

	@Operation(summary = "Delete a group", responses = {
		@ApiResponse(responseCode = "204", description = "No Content"),
		@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = Problem.class)))
	})
	@DeleteMapping("/{groupId}")
	public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId) {
		groupService.deleteGroup(groupId);

		return ResponseEntity.noContent().build();
	}
}
