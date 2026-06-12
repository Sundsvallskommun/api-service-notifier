package se.sundsvall.notifier.messaging.integration.db;

import feign.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import se.sundsvall.notifier.messaging.integration.db.model.OrganizationEntity;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long> {
	Optional<OrganizationEntity> findByOrgId(String orgId);

	List<OrganizationEntity> findByOrgIdIn(List<String> orgId);

	@Query(value = """
		WITH RECURSIVE org_tree AS (
		    SELECT o.*
		    FROM organization o
		    WHERE o.org_id = :orgId

		    UNION ALL

		    SELECT child.*
		    FROM organization child
		    JOIN org_tree t
		      ON child.parent_org_id = t.org_id
		)
		SELECT * FROM org_tree
		""", nativeQuery = true)
	List<OrganizationEntity> findOrgWithChildrenAndDescendants(@Param("orgId") String orgId);

	@Query(value = """
		SELECT o.*
		FROM organization o
		WHERE o.org_id = :orgId

		UNION ALL

		SELECT child.*
		FROM organization child
		WHERE child.parent_org_id = :orgId
		""", nativeQuery = true)
	List<OrganizationEntity> findOrgAndChildren(@Param("orgId") String orgId);

	@Query(value = """
		SELECT child.*
		FROM organization child
		WHERE child.parent_org_id = :orgId
		""", nativeQuery = true)
	List<OrganizationEntity> findChildren(@Param("orgId") String orgId);

	@Query("""
		select o
		from OrganizationEntity o
		where o.name like concat('%', :name, '%')
		    and o.treeLevel = (
		    select max(o2.treeLevel)
		    from OrganizationEntity o2
		    where o2.name = o.name and o2.name like concat('%', :name, '%'))
		""")
	Page<OrganizationEntity> findByNameContaining(@Param("name") String name, Pageable pageable);

}
