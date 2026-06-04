package se.sundsvall.notifier.messaging.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.notifier.messaging.api.model.response.OrganizationResponse;
import se.sundsvall.notifier.messaging.integration.db.entity.Organization;
import se.sundsvall.notifier.messaging.integration.db.repository.EmployeeRepository;
import se.sundsvall.notifier.messaging.integration.db.repository.OrganizationRepository;
import se.sundsvall.notifier.messaging.service.mapper.EntityToResponseMapper;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class OrganizationService {

	private final EntityToResponseMapper mapper;
	private final OrganizationRepository organizationRepository;
	private final EmployeeRepository employeeRepository;

	public OrganizationService(EntityToResponseMapper mapper, OrganizationRepository organizationRepository, EmployeeRepository employeeRepository) {
		this.mapper = mapper;
		this.organizationRepository = organizationRepository;
		this.employeeRepository = employeeRepository;
	}

	public List<OrganizationResponse> getAllOrganizations() {
		return organizationRepository.findAll()
			.stream().map(mapper::mapToOrganizationResponse).toList();
	}

	public OrganizationResponse getSpecificOrg(String orgId) {
		if (orgId == null) {
			throw new IllegalArgumentException("orgid is required");
		}

		return organizationRepository.findByOrgId(orgId).map(mapper::mapToOrganizationResponse)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "orgnanization with id '%s' not found".formatted(orgId)));
	}

	public List<OrganizationResponse> getOrgsById(List<String> orgId) {
		if (orgId == null || orgId.isEmpty()) {
			throw new IllegalArgumentException("orgid is required");
		}
		var result = organizationRepository.findByOrgIdIn(orgId).stream().map(mapper::mapToOrganizationResponse).toList();

		if (result.isEmpty()) {
			throw Problem.valueOf(NOT_FOUND, "No organization found.");
		}
		return result;
	}

	public List<OrganizationResponse> getOrgChildrenAndDescendantsWithId(String orgId) {
		var result = organizationRepository.findOrgWithChildrenAndDescendants(orgId).stream().map(mapper::mapToOrganizationResponse).toList();

		if (result.isEmpty()) {
			throw Problem.valueOf(NOT_FOUND, "No organization with id '%s' could be found".formatted(orgId));
		}

		return result;
	}

	public List<OrganizationResponse> getOrgAndChildrenWithId(String orgId) {
		var result = organizationRepository.findOrgAndChildren(orgId).stream().map(mapper::mapToOrganizationResponse).toList();

		if (result.isEmpty()) {
			throw Problem.valueOf(NOT_FOUND, "No organization with id '%s' could be found".formatted(orgId));
		}

		return result;
	}

	public Page<OrganizationResponse> getOrganizationWithSearch(String search, Pageable pageable) {
		String searchLowercase = search.trim().toLowerCase();
		Page<Organization> searchResult = organizationRepository.findByNameContaining(searchLowercase, pageable);
		return searchResult.map(mapper::mapToOrganizationResponse);
	}

	public List<OrganizationResponse> getChildrenReplaceDuplicateDescendantsWithRoot(String orgId) {
		List<Organization> directChildren = organizationRepository.findChildren(orgId);

		if (directChildren.isEmpty()) {
			throw Problem.valueOf(NOT_FOUND, "No children for organization with id '%s' could be found".formatted(orgId));
		}

		List<OrganizationResponse> response = new ArrayList<>();
		for (Organization topChild : directChildren) {
			if (topChild.getChildren().stream().noneMatch(child -> child.getName().equals(topChild.getName()))) {
				response.add(mapper.mapToOrganizationResponse(topChild));
				continue;
			}
			Organization triggeredNode = findLastDuplicateBeforeBranch(topChild);

			List<Organization> resolvedChildren = organizationRepository.findChildren(triggeredNode.getOrgId());
			if (resolvedChildren.isEmpty()) {
				response.add(mapper.mapToOrganizationResponse(triggeredNode));
			} else {
				for (Organization child : resolvedChildren) {
					if (!child.getChildren().isEmpty() || !employeeRepository.findByOrgId(child.getOrgId()).isEmpty()) {
						response.add(mapper.mapToOrganizationResponse(child));
					}
				}
			}
		}
		return response;
	}

	private Organization findLastDuplicateBeforeBranch(Organization start) {
		String firstOrg = start.getName();
		Organization current = start;
		while (Objects.equals(firstOrg, current.getName())) {
			List<Organization> children = organizationRepository.findChildren(current.getOrgId());

			if (children.isEmpty()) {
				return current;
			}
			if (children.size() > 1) {
				return current;
			}
			current = children.getFirst();
		}
		return current;
	}
}
