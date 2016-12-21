package dk.os2opgavefordeler.employment;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;

import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Municipality;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

@Repository(forEntity = Employment.class)
@Transactional
public abstract class EmploymentRepository extends AbstractEntityRepository<Employment, Long> {

    public abstract Employment findByEmail(String email);

    public abstract Employment findByEmailAndMunicipality(String email, Municipality municipality);

}
