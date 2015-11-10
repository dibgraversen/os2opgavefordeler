package dk.os2opgavefordeler.employment;

import dk.os2opgavefordeler.model.OrgUnit;
import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;

@Repository(forEntity = OrgUnit.class)
public abstract class OrgUnitRepository extends AbstractEntityRepository<OrgUnit, Long> {

}