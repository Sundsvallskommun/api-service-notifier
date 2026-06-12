package se.sundsvall.notifier.messaging.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.notifier.messaging.api.model.response.OrganizationResponse;
import se.sundsvall.notifier.messaging.integration.db.EmployeeRepository;
import se.sundsvall.notifier.messaging.integration.db.OrganizationRepository;
import se.sundsvall.notifier.messaging.integration.db.model.OrganizationEntity;
import se.sundsvall.notifier.messaging.service.mapper.EntityToResponseMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrganizationServiceTest {
	@Mock
	private EntityToResponseMapper mapper;

	@Mock
	private OrganizationRepository organizationRepository;
	@Mock
	private EmployeeRepository employeeRepository;

	@Test
	void getAllOrganizations() {
		var service = new OrganizationService(mapper, organizationRepository, employeeRepository);

		var org1 = new OrganizationEntity();
		var org2 = new OrganizationEntity();

		var response1 = mock(OrganizationResponse.class);
		var response2 = mock(OrganizationResponse.class);

		when(organizationRepository.findAll()).thenReturn(List.of(org1, org2));
		when(mapper.mapToOrganizationResponse(org1)).thenReturn(response1);
		when(mapper.mapToOrganizationResponse(org2)).thenReturn(response2);

		var result = service.getAllOrganizations();

		assertThat(List.of(response1, response2)).isEqualTo(result);
		verify(organizationRepository).findAll();
		verify(mapper).mapToOrganizationResponse(org1);
		verify(mapper).mapToOrganizationResponse(org2);
	}

	@Test
	void getSpecificOrg_test() {

		var service = new OrganizationService(mapper, organizationRepository, employeeRepository);

		var org = new OrganizationEntity();
		var response = mock(OrganizationResponse.class);

		when(organizationRepository.findByOrgId("orgId")).thenReturn(Optional.of(org));
		when(mapper.mapToOrganizationResponse(org)).thenReturn(response);

		var result = service.getSpecificOrg("orgId");

		assertThat(result).isEqualTo(response);
	}

	@Test
	void getOrgsByIds_test() {
		var service = new OrganizationService(mapper, organizationRepository, employeeRepository);

		var org1 = new OrganizationEntity();
		var org2 = new OrganizationEntity();

		var response1 = mock(OrganizationResponse.class);
		var response2 = mock(OrganizationResponse.class);

		when(organizationRepository.findByOrgIdIn(List.of("Id1", "Id2"))).thenReturn(List.of(org1, org2));
		when(mapper.mapToOrganizationResponse(org1)).thenReturn(response1);
		when(mapper.mapToOrganizationResponse(org2)).thenReturn(response2);

		var result = service.getOrgsById(List.of("Id1", "Id2"));

		assertThat(List.of(response1, response2)).isEqualTo(result);
	}

	@Test
	void getOrgChildrenAndDescendantsWithId_test() {
		var service = new OrganizationService(mapper, organizationRepository, employeeRepository);

		var org1 = new OrganizationEntity();
		var org2 = new OrganizationEntity();

		var response1 = mock(OrganizationResponse.class);
		var response2 = mock(OrganizationResponse.class);

		when(organizationRepository.findOrgWithChildrenAndDescendants("Id1")).thenReturn(List.of(org1, org2));
		when(mapper.mapToOrganizationResponse(org1)).thenReturn(response1);
		when(mapper.mapToOrganizationResponse(org2)).thenReturn(response2);

		var result = service.getOrgChildrenAndDescendantsWithId("Id1");

		assertThat(List.of(response1, response2)).isEqualTo(result);
	}

	@Test
	void getOrgAndChildrenWithId_test() {
		var service = new OrganizationService(mapper, organizationRepository, employeeRepository);

		var org1 = new OrganizationEntity();
		var org2 = new OrganizationEntity();

		var response1 = mock(OrganizationResponse.class);
		var response2 = mock(OrganizationResponse.class);

		when(organizationRepository.findOrgAndChildren("Id1")).thenReturn(List.of(org1, org2));
		when(mapper.mapToOrganizationResponse(org1)).thenReturn(response1);
		when(mapper.mapToOrganizationResponse(org2)).thenReturn(response2);

		var result = service.getOrgAndChildrenWithId("Id1");

		assertThat(List.of(response1, response2)).isEqualTo(result);
	}

	@Test
	void getOrgsByIds_Null_test() {
		var service = new OrganizationService(mapper, organizationRepository, employeeRepository);

		var exception = assertThrows(IllegalArgumentException.class, () -> service.getOrgsById(null));

		assertThat(exception.getMessage()).isEqualTo("orgid is required");
		verifyNoInteractions(organizationRepository, mapper);
	}

	@Test
	void getSpecificOrg_Id_Null_test() {
		var service = new OrganizationService(mapper, organizationRepository, employeeRepository);

		var exception = assertThrows(IllegalArgumentException.class, () -> service.getSpecificOrg(null));

		assertThat(exception.getMessage()).isEqualTo("orgid is required");
		verifyNoInteractions(organizationRepository, mapper);
	}

	@Test
	void getOrgChildrenAndDescendantsWithId_nothing_found_test() {
		var service = new OrganizationService(mapper, organizationRepository, employeeRepository);

		when(organizationRepository.findOrgWithChildrenAndDescendants("1"))
			.thenReturn(List.of());

		var exception = assertThrows(ThrowableProblem.class,
			() -> service.getOrgChildrenAndDescendantsWithId("1"));

		assertThat(exception.getMessage()).contains("No organization with id '1' could be found");
		verify(organizationRepository).findOrgWithChildrenAndDescendants("1");
		verifyNoInteractions(mapper);
	}

	@Test
	void getOrgAndChildrenWithId_nothing_found_test() {
		var service = new OrganizationService(mapper, organizationRepository, employeeRepository);

		when(organizationRepository.findOrgAndChildren("1"))
			.thenReturn(List.of());

		var exception = assertThrows(ThrowableProblem.class,
			() -> service.getOrgAndChildrenWithId("1"));

		assertThat(exception.getMessage()).contains("No organization with id '1' could be found");
		verify(organizationRepository).findOrgAndChildren("1");
		verifyNoInteractions(mapper);
	}

	@Test
	void getOrgsById_nothing_found_test() {
		var service = new OrganizationService(mapper, organizationRepository, employeeRepository);

		when(organizationRepository.findByOrgIdIn(List.of("1")))
			.thenReturn(List.of());

		var exception = assertThrows(ThrowableProblem.class,
			() -> service.getOrgsById(List.of("1")));

		assertThat(exception.getMessage()).contains("No organization found.");
		verify(organizationRepository).findByOrgIdIn(List.of("1"));
		verifyNoInteractions(mapper);
	}

	@Test
	void getOrganizationWithSearch_test() {
		var service = new OrganizationService(mapper, organizationRepository, employeeRepository);

		var org1 = new OrganizationEntity();
		var org2 = new OrganizationEntity();
		var response1 = mock(OrganizationResponse.class);
		var response2 = mock(OrganizationResponse.class);

		Pageable pageable = PageRequest.of(0, 2);
		Page<OrganizationEntity> result = new PageImpl<>(List.of(org1, org2), pageable, 2);

		when(organizationRepository.findByNameContaining("search", pageable)).thenReturn(result);
		when(mapper.mapToOrganizationResponse(org1)).thenReturn(response1);
		when(mapper.mapToOrganizationResponse(org2)).thenReturn(response2);

		var searchResult = service.getOrganizationWithSearch("search", pageable);

		assertThat(searchResult.getContent()).containsExactly(response1, response2);
	}

	@Test
	void getChildrenReplaceDuplicateDescendantsWithRoot_test() {
		var service = new OrganizationService(mapper, organizationRepository, employeeRepository);

		var top = new OrganizationEntity();
		top.setOrgId("orgIdTop");
		top.setParentOrgId("parentOrgId");
		top.setName("duplicateOrg");
		top.setTreeLevel(2);

		var middle = new OrganizationEntity();
		middle.setOrgId("orgIdMiddle");
		middle.setParentOrgId("orgIdTop");
		middle.setName("duplicateOrg");
		middle.setTreeLevel(3);

		var bottom = new OrganizationEntity();
		bottom.setOrgId("orgIdBottom");
		bottom.setParentOrgId("orgIdMiddle");
		bottom.setName("duplicateOrg");
		bottom.setTreeLevel(4);
		bottom.setChildren(Set.of());

		top.setChildren(Set.of(middle));

		var bottomResponse = OrganizationResponse.builder()
			.withOrgId(bottom.getOrgId())
			.withParentOrgId(bottom.getParentOrgId())
			.withTreeLevel(bottom.getTreeLevel())
			.withName(bottom.getName())
			.build();

		when(mapper.mapToOrganizationResponse(bottom)).thenReturn(bottomResponse);
		when(organizationRepository.findChildren("parentOrgId")).thenReturn(List.of(top));
		when(organizationRepository.findChildren("orgIdTop")).thenReturn(List.of(middle));
		when(organizationRepository.findChildren("orgIdMiddle")).thenReturn(List.of(bottom));
		when(organizationRepository.findChildren("orgIdBottom")).thenReturn(List.of());

		var result = service.getChildrenReplaceDuplicateDescendantsWithRoot("parentOrgId");
		var remainingOrg = result.getFirst();

		assertThat(result).hasSize(1);
		assertThat(remainingOrg.orgId()).isEqualTo(bottom.getOrgId());
		assertThat(remainingOrg.parentOrgId()).isEqualTo(bottom.getParentOrgId());
		assertThat(remainingOrg.name()).isEqualTo(bottom.getName());

		verify(mapper).mapToOrganizationResponse(bottom);
		verifyNoMoreInteractions(mapper);
		verifyNoInteractions(employeeRepository);
	}

	@Test
	void getChildrenReplaceDuplicateDescendantsWithRoot_addsResolvedChildWhenChildHasChildren() {
		var service = new OrganizationService(mapper, organizationRepository, employeeRepository);

		var top = new OrganizationEntity();
		top.setOrgId("orgIdTop");
		top.setParentOrgId("parentOrgId");
		top.setName("duplicateOrg");

		var middle = new OrganizationEntity();
		middle.setOrgId("orgIdMiddle");
		middle.setParentOrgId("orgIdTop");
		middle.setName("duplicateOrg");

		var bottom = new OrganizationEntity();
		bottom.setOrgId("orgIdChild");
		bottom.setParentOrgId("orgIdMiddle");
		bottom.setName("duplicateOrg");
		bottom.setChildren(Set.of());

		var branch = new OrganizationEntity();
		branch.setOrgId("orgIdBranch");
		branch.setParentOrgId("orgIdMiddle");
		branch.setName("branch");

		var branchChild = new OrganizationEntity();
		branchChild.setOrgId("orgIdBranchChild");
		branchChild.setParentOrgId("orgIdBranch");
		branchChild.setName("branchChild");

		top.setChildren(Set.of(middle));
		branch.setChildren(Set.of(branchChild));

		var response = OrganizationResponse.builder()
			.withOrgId(branch.getOrgId())
			.withParentOrgId(branch.getParentOrgId())
			.withTreeLevel(branch.getTreeLevel())
			.withName(branch.getName())
			.build();

		when(organizationRepository.findChildren("parentOrgId")).thenReturn(List.of(top));
		when(organizationRepository.findChildren("orgIdTop")).thenReturn(List.of(middle));
		when(organizationRepository.findChildren("orgIdMiddle")).thenReturn(List.of(bottom, branch));
		when(employeeRepository.findByOrgId("orgIdChild")).thenReturn(List.of());
		when(mapper.mapToOrganizationResponse(branch)).thenReturn(response);

		var result = service.getChildrenReplaceDuplicateDescendantsWithRoot("parentOrgId");

		assertThat(result).hasSize(1);
		assertThat(result.getFirst().orgId()).isEqualTo("orgIdBranch");
		assertThat(result.getFirst().name()).isEqualTo("branch");

		verify(mapper).mapToOrganizationResponse(branch);
		verifyNoMoreInteractions(mapper);
		verify(employeeRepository).findByOrgId("orgIdChild");
	}
}
