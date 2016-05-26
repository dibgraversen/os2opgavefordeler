package dk.os2opgavefordeler.Kle;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;

import dk.os2opgavefordeler.model.Kle;

@Repository(forEntity = Kle.class)
public abstract class KleRepository extends AbstractEntityRepository<Kle, Long> {

}