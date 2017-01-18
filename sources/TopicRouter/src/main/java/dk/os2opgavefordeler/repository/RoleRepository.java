package dk.os2opgavefordeler.repository;

import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Role;
import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

import java.util.List;

/**
 * @author hlo@miracle.dk
 */
@Repository(forEntity = Role.class)
@Transactional
public abstract class RoleRepository extends AbstractEntityRepository<Role, Long> {

	public abstract List<Role> findByEmployment(Employment employment);
}
