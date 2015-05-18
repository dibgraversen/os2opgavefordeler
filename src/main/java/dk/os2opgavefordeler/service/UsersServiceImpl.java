package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.model.presentation.RolePO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hlo@miracle.dk
 */
@Stateless
public class UsersServiceImpl implements UsersService {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@PersistenceContext(unitName = "OS2TopicRouter")
	private EntityManager em;

	@Override
	public List<RolePO> getRoles(long userId) {
		List<RolePO> result = new ArrayList<>();
		Query query = em.createQuery("SELECT r FROM Role r WHERE userId = :userId");
		query.setParameter("userId", userId);
		final List<Role> roles = query.getResultList();
		for (Role role : roles) {
			result.add(new RolePO(role));
		}
		return result;
	}

	@Override
	public void createRole(Role role) {
		em.persist(role);
	}

}
