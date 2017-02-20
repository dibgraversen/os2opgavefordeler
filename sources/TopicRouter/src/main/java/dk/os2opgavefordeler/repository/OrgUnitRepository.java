package dk.os2opgavefordeler.repository;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.SingleResultType;

import java.util.List;

import dk.os2opgavefordeler.model.OrgUnit;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

@Repository(forEntity = OrgUnit.class)
@Transactional
public abstract class OrgUnitRepository extends AbstractEntityRepository<OrgUnit, Long> {

	@Query(value = "SELECT ou FROM OrgUnit ou WHERE ou.businessKey = ?1 AND ou.municipality.id = ?2", singleResult = SingleResultType.OPTIONAL)
	public abstract OrgUnit findByBusinessKeyAndMunicipalityId(String businessKey, long municipalityId);

	@Query(value = "SELECT ou FROM OrgUnit ou WHERE ou.pNumber = ?1 AND ou.municipality.id = ?2")
	public abstract OrgUnit findByPNumberAndMunicipalityId(String pNumber, long municipalityId);

	@Query(value = "SELECT ou FROM OrgUnit ou WHERE ou.municipality.id = ?1")
	public abstract List<OrgUnit> findByMunicipality(long municipalityId);

	@Query("select org from OrgUnit org where org.isActive = false and org.municipality.id = ?1")
	public abstract List<OrgUnit> findOrgsToDelete(Long municipalityId);

	@Modifying
	@Query("update OrgUnit org set org.manager = null where org in ( ?1 )")
	public abstract void clearManager(List<OrgUnit> orgUnits);

	@Modifying
	@Query("update OrgUnit ou set ou.isActive = false where ou.municipality.id = ?1")
	public abstract void deactivateForMunicipality(long municipalityId);

	@Modifying
	@Query("update OrgUnit org set org.parent = null where org in ( ?1 )")
	public abstract void clearParents(List<OrgUnit> orgUnits);
}
