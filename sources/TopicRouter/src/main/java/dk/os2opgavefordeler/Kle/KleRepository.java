package dk.os2opgavefordeler.Kle;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;

import dk.os2opgavefordeler.model.Kle;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

@Repository(forEntity = Kle.class)
@Transactional
public abstract class KleRepository extends AbstractEntityRepository<Kle, Long> {

}