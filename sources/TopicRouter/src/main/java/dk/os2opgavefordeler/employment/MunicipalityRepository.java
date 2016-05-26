package dk.os2opgavefordeler.employment;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;

import dk.os2opgavefordeler.model.Municipality;

@Repository(forEntity = Municipality.class)
public abstract class MunicipalityRepository extends AbstractEntityRepository<Municipality, Long> {

    public abstract Municipality findByName(String name);

}