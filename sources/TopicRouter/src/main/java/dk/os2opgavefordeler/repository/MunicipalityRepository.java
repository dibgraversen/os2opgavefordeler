package dk.os2opgavefordeler.repository;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;

import dk.os2opgavefordeler.model.Municipality;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

@Repository(forEntity = Municipality.class)
@Transactional
public abstract class MunicipalityRepository extends AbstractEntityRepository<Municipality, Long> {

    public abstract Municipality findByName(String name);

}