package se.sundsvall.notifier.messaging.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.notifier.messaging.api.model.request.GroupRequest;
import se.sundsvall.notifier.messaging.api.model.request.GroupUpdateRequest;
import se.sundsvall.notifier.messaging.api.model.response.GroupResponse;
import se.sundsvall.notifier.messaging.integration.db.entity.Group;
import se.sundsvall.notifier.messaging.integration.db.repository.EmployeeRepository;
import se.sundsvall.notifier.messaging.integration.db.repository.GroupRepository;
import se.sundsvall.notifier.messaging.service.mapper.EntityToResponseMapper;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class GroupService {
	private final GroupRepository groupRepository;
	private final EmployeeRepository employeeRepository;
	private final EntityToResponseMapper mapper;

	private static final String GROUP_NOT_FOUND = "Group with id '%s' not found";

	public GroupService(GroupRepository groupRepo, EmployeeRepository employeeRepository, EntityToResponseMapper mapper) {
		this.groupRepository = groupRepo;
		this.employeeRepository = employeeRepository;
		this.mapper = mapper;

	}

	public List<GroupResponse> getAllGroups() {
		List<Group> groups = groupRepository.findAll();
		List<GroupResponse> groupList = new ArrayList<>();

		for (Group group : groups) {
			GroupResponse groupResponse = mapper.mapToGroupResponse(group);
			groupList.add(groupResponse);
		}
		return groupList;
	}

	public List<GroupResponse> getGroupsByCreatorId(String creatorId) {
		return groupRepository.findAllByCreatorId(creatorId).stream()
			.sorted(Comparator.comparing(Group::getId))
			.map(mapper::mapToGroupResponse)
			.toList();
	}

	public GroupResponse getGroupById(Long id) {
		var group = groupRepository.findById(id).orElseThrow(() -> Problem.valueOf(NOT_FOUND, GROUP_NOT_FOUND.formatted(id)));

		return mapper.mapToGroupResponse(group);
	}

	@Transactional
	public Long createGroup(GroupRequest request) {
		var group = Group.builder()
			.withName(request.name())
			.withDescription(request.description())
			.withCreatorId(request.creatorId())
			.withEmployees(employeeRepository.findAllByIdIn(request.employees()))
			.build();

		return groupRepository.save(group).getId();
	}

	@Transactional
	public GroupResponse updateGroup(Long id, GroupUpdateRequest request) {
		var existingGroup = groupRepository.findById(id)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, GROUP_NOT_FOUND.formatted(id)));

		existingGroup.setName(request.name());
		existingGroup.setDescription(request.description());
		existingGroup.setEmployees(employeeRepository.findAllByIdIn(request.employees()));

		var savedGroup = groupRepository.save(existingGroup);
		return mapper.mapToGroupResponse(savedGroup);
	}

	public void deleteGroup(Long id) {
		var group = groupRepository.findById(id).orElseThrow(() -> Problem.valueOf(NOT_FOUND, GROUP_NOT_FOUND.formatted(id)));

		groupRepository.deleteById(group.getId());
	}

}
