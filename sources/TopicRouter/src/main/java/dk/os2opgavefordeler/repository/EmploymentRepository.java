package dk.os2opgavefordeler.repository;

import dk.os2opgavefordeler.model.OrgUnit;
import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;

import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Municipality;
import org.apache.deltaspike.data.api.SingleResultType;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

import java.util.List;

@Repository(forEntity = Employment.class)
@Transactional
public abstract class EmploymentRepository extends AbstractEntityRepository<Employment, Long> {

	public abstract Employment findByEmail(String email);

	public abstract Employment findByEmailAndMunicipality(String email, Municipality municipality);

	@Query(value = "SELECT repository FROM Employment repository WHERE repository.businessKey = ?1 AND repository.municipality.id = ?2", singleResult = SingleResultType.OPTIONAL)
	public abstract Employment findByBusinessKeyAndMunicipalityId(String businessKey, Long municipalityId);

	@Query("select emp from Employment emp where emp.isActive = false and emp.municipality.id = ?1 ")
	public abstract List<Employment> findEmploymentsToDelete(Long municipalityId);

	@Modifying
	@Query("update Employment emp set emp.employedIn = null where emp.employedIn in ( ?1 )")
	public abstract void clearEmployedIn(List<OrgUnit> orgUnits);


}
