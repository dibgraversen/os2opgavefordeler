package dk.os2opgavefordeler.employment;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
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

    @Query(value = "SELECT ou FROM OrgUnit ou WHERE ou.municipality.id = ?1")
    public abstract List<OrgUnit> findByMunicipality(long municipalityId);

}
