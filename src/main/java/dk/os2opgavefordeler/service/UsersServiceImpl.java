package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.model.UserSettings;
import dk.os2opgavefordeler.model.presentation.RolePO;
import dk.os2opgavefordeler.model.presentation.UserSettingsPO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author hlo@miracle.dk
 */
@Stateless
public class UsersServiceImpl implements UsersService {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@PersistenceContext(unitName = "OS2TopicRouter")
	private EntityManager em;

	@Override
	public Optional<User> findById(int userId) {
		throw new NotImplementedException();
	}

	@Override
	public Optional<User> findByEmail(String email) {
		throw new NotImplementedException();
	}

	@Override
	public User createUser(User user) {
		throw new NotImplementedException();
	}

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

	@Override
	public UserSettingsPO getSettings(long userId) {
		// TODO what if not created?
		Query query = em.createQuery("SELECT u FROM UserSettings u WHERE userId = :userId");
		query.setParameter("userId", userId);
		UserSettings settings = (UserSettings) query.getSingleResult();
		return new UserSettingsPO(settings);
	}

	@Override
	public void createUserSettings(UserSettings userSettings) {
		em.persist(userSettings);
	}

	@Override
	public void updateSettings(UserSettingsPO updatedsettings) {
		UserSettings settings = em.find(UserSettings.class, updatedsettings.getId());
		settings.setScope(updatedsettings.getScope());
		settings.setShowResponsible(updatedsettings.isShowResponsible());
		settings.setShowExpandedOrg(updatedsettings.isShowExpandedOrg());
		em.merge(settings);
	}

}
