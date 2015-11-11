package dk.os2opgavefordeler.employment;

import dk.os2opgavefordeler.model.Employment;
import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;

@Repository(forEntity = Employment.class)
public abstract class EmploymentRepository extends AbstractEntityRepository<Employment, Long> {

    public abstract Employment findByEmail(String email);

}
