package dk.os2opgavefordeler.employment;

import dk.os2opgavefordeler.model.Municipality;
import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;

@Repository(forEntity = Municipality.class)
public abstract class MunicipalityRepository extends AbstractEntityRepository<Municipality, Long> {

    public abstract Municipality findByName(String name);

}