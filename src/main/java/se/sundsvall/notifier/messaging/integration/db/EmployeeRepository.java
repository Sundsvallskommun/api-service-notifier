package se.sundsvall.notifier.messaging.integration.db;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.sundsvall.notifier.messaging.integration.db.model.EmployeeEntity;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

	List<EmployeeEntity> findByActiveEmployeeTrue();

	Page<EmployeeEntity> findByActiveEmployeeTrue(Pageable pageable);

	Set<EmployeeEntity> findAllByIdIn(Set<Long> ids);

	Set<EmployeeEntity> findAllByIdInAndActiveEmployeeTrue(Set<Long> ids);

	List<EmployeeEntity> findByOrgId(String orgId);

	List<EmployeeEntity> findByOrgIdAndActiveEmployeeTrue(String orgId);

	List<EmployeeEntity> findByOrgIdIn(List<String> orgId);

	List<EmployeeEntity> findByOrgIdInAndActiveEmployeeTrue(List<String> orgId);

	@Query("""
		select e
		from EmployeeEntity e
		where e.activeEmployee = true
		    and(
		        :s2 is null
		            and (
		                e.firstName like concat(:s1,'%')
		                or e.lastName like concat(:s1,'%')
		                or e.workTitle like concat(:s1,'%')
		                )
		        or
		        :s2 is not null
		            and
		                e.firstName like concat(:s1,'%') and e.lastName like concat(:s2,'%')
		                or e.lastName like concat(:s1,'%') and e.firstName like concat(:s2,'%')
		                or e.firstName like concat(:s1,'%') and e.workTitle like concat(:s2,'%')
		                or e.workTitle like concat(:s1,'%') and e.firstName like concat(:s2,'%')
		)""")
	Page<EmployeeEntity> findMatchingEmployee(@Param("s1") String searchTerm1, @Param("s2") String searchTerm2, Pageable page);

	List<EmployeeEntity> findAllByManagerCodeIsNotNull();

	List<EmployeeEntity> findAllByManagerCodeIsNotNullAndActiveEmployeeTrue();

}
