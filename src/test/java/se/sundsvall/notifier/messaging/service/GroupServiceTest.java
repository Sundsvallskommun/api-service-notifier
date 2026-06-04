package se.sundsvall.notifier.messaging.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.notifier.messaging.api.model.request.GroupRequest;
import se.sundsvall.notifier.messaging.api.model.request.GroupUpdateRequest;
import se.sundsvall.notifier.messaging.api.model.response.GroupResponse;
import se.sundsvall.notifier.messaging.integration.db.entity.Employee;
import se.sundsvall.notifier.messaging.integration.db.entity.Group;
import se.sundsvall.notifier.messaging.integration.db.repository.EmployeeRepository;
import se.sundsvall.notifier.messaging.integration.db.repository.GroupRepository;
import se.sundsvall.notifier.messaging.service.mapper.EntityToResponseMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {

	@Mock
	private EntityToResponseMapper mapper;

	@Mock
	private GroupRepository groupRepositoryMock;

	@Mock
	private EmployeeRepository employeeRepositoryMock;

	@Captor
	ArgumentCaptor<Group> groupCaptor;

	@InjectMocks
	private GroupService groupService;

	@Test
	void getAllGroups_noGroupFound() {
		when(groupRepositoryMock.findAll()).thenReturn(List.of());

		List<GroupResponse> result = groupService.getAllGroups();

		assertThat(result).isEmpty();

		verify(groupRepositoryMock).findAll();
		verifyNoInteractions(employeeRepositoryMock, mapper);
		verifyNoMoreInteractions(groupRepositoryMock);
	}

	@Test
	void getAllGroups_groupsFound() {
		var group1 = new Group();
		group1.setId(1L);

		var group2 = new Group();
		group2.setId(2L);

		when(groupRepositoryMock.findAll()).thenReturn(List.of(group1, group2));

		var response1 = GroupResponse.builder().withId(1L).withName("GroupName1").build();
		var response2 = GroupResponse.builder().withId(2L).withName("GroupName2").build();

		when(mapper.mapToGroupResponse(group1)).thenReturn(response1);
		when(mapper.mapToGroupResponse(group2)).thenReturn(response2);

		List<GroupResponse> result = groupService.getAllGroups();

		assertThat(result).containsExactly(response1, response2);
		verify(groupRepositoryMock).findAll();
		verify(mapper).mapToGroupResponse(group1);
		verify(mapper).mapToGroupResponse(group2);
		verifyNoInteractions(employeeRepositoryMock);
		verifyNoMoreInteractions(groupRepositoryMock, mapper);
	}

	@Test
	void getGroupsByCreatorId_noGroupsFound() {
		when(groupRepositoryMock.findAllByCreatorId("creatorId")).thenReturn(List.of());

		List<GroupResponse> result = groupService.getGroupsByCreatorId("creatorId");

		assertThat(result).isEmpty();
		verify(groupRepositoryMock).findAllByCreatorId("creatorId");
		verifyNoInteractions(employeeRepositoryMock, mapper);
		verifyNoMoreInteractions(groupRepositoryMock);
	}

	@Test
	void getGroupsByCreatorId_groupsFound() {
		var creatorId = "creatorId";

		var group1 = new Group();
		group1.setId(1L);
		group1.setCreatorId(creatorId);

		var group2 = new Group();
		group2.setId(2L);
		group2.setCreatorId(creatorId);

		when(groupRepositoryMock.findAllByCreatorId(creatorId)).thenReturn(List.of(group1, group2));

		var response1 = GroupResponse.builder().withId(1L).withCreatorId(creatorId).build();
		var response2 = GroupResponse.builder().withId(2L).withCreatorId(creatorId).build();

		when(mapper.mapToGroupResponse(group1)).thenReturn(response1);
		when(mapper.mapToGroupResponse(group2)).thenReturn(response2);

		var result = groupService.getGroupsByCreatorId(creatorId);

		assertThat(result).containsExactly(response1, response2);
		verify(groupRepositoryMock).findAllByCreatorId(creatorId);
		verify(mapper).mapToGroupResponse(group1);
		verify(mapper).mapToGroupResponse(group2);
		verifyNoInteractions(employeeRepositoryMock);
		verifyNoMoreInteractions(groupRepositoryMock, mapper);

	}

	@Test
	void getGroupById_groupFound() {
		var groupId = 1L;
		var group = new Group();
		group.setId(groupId);

		when(groupRepositoryMock.findById(groupId)).thenReturn(Optional.of(group));

		var response = GroupResponse.builder().withId(groupId).build();
		when(mapper.mapToGroupResponse(group)).thenReturn(response);

		var result = groupService.getGroupById(groupId);

		assertEquals(response, result);
		verify(groupRepositoryMock).findById(groupId);
		verify(mapper).mapToGroupResponse(group);
		verifyNoInteractions(employeeRepositoryMock);
		verifyNoMoreInteractions(groupRepositoryMock, mapper);
	}

	@Test
	void getGroupById_groupNotFound() {
		var groupId = 1L;
		when(groupRepositoryMock.findById(groupId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> groupService.getGroupById(groupId))
			.isInstanceOf(ThrowableProblem.class)
			.satisfies(ex -> {
				var p = (ThrowableProblem) ex;
				assertThat(p.getStatus()).isEqualTo(NOT_FOUND);
				assertThat(p.getDetail()).isEqualTo("Group with id '1' not found");
			});

		verify(groupRepositoryMock).findById(groupId);
		verifyNoInteractions(employeeRepositoryMock, mapper);
		verifyNoMoreInteractions(groupRepositoryMock);
	}

	@Test
	void createGroup_saveGroup_returnId() {
		var request = GroupRequest.builder()
			.withName("Team A")
			.withDescription("Beskrivning")
			.withCreatorId("creator-123")
			.withEmployees(Set.of(10L, 20L))
			.build();

		var e1 = mock(Employee.class);
		var e2 = mock(Employee.class);
		var employees = Set.of(e1, e2);

		when(employeeRepositoryMock.findAllByIdIn(request.employees())).thenReturn(employees);

		var saved = new Group();
		saved.setId(123L);
		when(groupRepositoryMock.save(any(Group.class))).thenReturn(saved);

		var id = groupService.createGroup(request);

		assertEquals(123L, id);

		verify(employeeRepositoryMock).findAllByIdIn(request.employees());
		verify(groupRepositoryMock).save(groupCaptor.capture());

		var groupToSave = groupCaptor.getValue();
		assertEquals("Team A", groupToSave.getName());
		assertEquals("Beskrivning", groupToSave.getDescription());
		assertEquals("creator-123", groupToSave.getCreatorId());
		assertEquals(employees, groupToSave.getEmployees());

		verifyNoInteractions(mapper);
		verifyNoMoreInteractions(employeeRepositoryMock, groupRepositoryMock, mapper);
	}

	@Test
	void updateGroup_notFound() {
		var groupId = 1L;
		var request = GroupUpdateRequest.builder()
			.withName("New name")
			.withDescription("New description")
			.withEmployees(Set.of(10L, 20L))
			.build();

		when(groupRepositoryMock.findById(groupId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> groupService.updateGroup(groupId, request))
			.isInstanceOf(ThrowableProblem.class)
			.satisfies(ex -> {
				var problem = (ThrowableProblem) ex;
				assertThat(problem.getStatus()).isEqualTo(NOT_FOUND);
				assertThat(problem.getDetail()).isEqualTo("Group with id '1' not found");
			});

		verify(groupRepositoryMock).findById(groupId);
		verifyNoInteractions(employeeRepositoryMock, mapper);
		verifyNoMoreInteractions(groupRepositoryMock);
	}

	@Test
	void updateGroup_found_updateAndReturnId() {
		var groupId = 1L;

		var existing = new Group();
		existing.setId(groupId);
		existing.setName("Old name");
		existing.setDescription("Old description");
		existing.setCreatorId("creator-123");

		when(groupRepositoryMock.findById(groupId)).thenReturn(Optional.of(existing));

		var request = GroupUpdateRequest.builder()
			.withName("New name")
			.withDescription("New description")
			.withEmployees(Set.of(10L, 20L))
			.build();

		var emp1 = mock(Employee.class);
		var emp2 = mock(Employee.class);
		var employees = Set.of(emp1, emp2);

		when(employeeRepositoryMock.findAllByIdIn(request.employees())).thenReturn(employees);

		when(groupRepositoryMock.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var response = GroupResponse.builder()
			.withId(groupId)
			.withName("New name")
			.withDescription("New description")
			.withCreatorId("creator-123")
			.build();

		when(mapper.mapToGroupResponse(any(Group.class))).thenReturn(response);

		var result = groupService.updateGroup(groupId, request);

		assertEquals(response, result);

		verify(groupRepositoryMock).findById(groupId);
		verify(employeeRepositoryMock).findAllByIdIn(request.employees());
		verify(groupRepositoryMock).save(groupCaptor.capture());

		var groupToSave = groupCaptor.getValue();

		verify(mapper).mapToGroupResponse(groupToSave);

		assertEquals(groupId, groupToSave.getId());
		assertEquals("New name", groupToSave.getName());
		assertEquals("New description", groupToSave.getDescription());
		assertEquals("creator-123", groupToSave.getCreatorId());
		assertEquals(employees, groupToSave.getEmployees());

		verifyNoMoreInteractions(employeeRepositoryMock, groupRepositoryMock, mapper);
	}

	@Test
	void deleteGroup_found() {
		var groupId = 1L;
		var group = new Group();
		group.setId(groupId);

		when(groupRepositoryMock.findById(groupId)).thenReturn(Optional.of(group));

		groupService.deleteGroup(groupId);

		verify(groupRepositoryMock).findById(groupId);
		verify(groupRepositoryMock).deleteById(groupId);
		verifyNoInteractions(employeeRepositoryMock, mapper);
		verifyNoMoreInteractions(groupRepositoryMock);
	}

	@Test
	void deleteGroup_notFound() {
		var groupId = 1L;
		when(groupRepositoryMock.findById(groupId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> groupService.deleteGroup(groupId))
			.isInstanceOf(ThrowableProblem.class)
			.satisfies(ex -> {
				var p = (ThrowableProblem) ex;
				assertThat(p.getStatus()).isEqualTo(NOT_FOUND);
				assertThat(p.getDetail()).isEqualTo("Group with id '1' not found");
			});

		verify(groupRepositoryMock).findById(groupId);
		verifyNoInteractions(employeeRepositoryMock, mapper);
		verifyNoMoreInteractions(groupRepositoryMock);
	}
}
