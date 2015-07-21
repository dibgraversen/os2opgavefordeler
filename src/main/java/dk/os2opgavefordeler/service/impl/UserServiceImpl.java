package dk.os2opgavefordeler.service.impl;

import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.model.UserSettings;
import dk.os2opgavefordeler.model.presentation.RolePO;
import dk.os2opgavefordeler.model.presentation.UserSettingsPO;
import dk.os2opgavefordeler.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author hlo@miracle.dk
 */
@Stateless
public class UserServiceImpl implements UserService {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@PersistenceContext(unitName = "OS2TopicRouter")
	private EntityManager em;

	@Inject
	private EmploymentService employmentService;

	@Override
	public Optional<User> findById(long userId) {
		TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.id = :userId", User.class);
		query.setParameter("userId", userId);

		try {
			return Optional.of(query.getSingleResult());
		}
		catch(NoResultException ex) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<User> findByEmail(String email) {
		TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
		query.setParameter("email", email);

		try {
			return Optional.of(query.getSingleResult());
		}
		catch(NoResultException ex) {
			return Optional.empty();
		}
	}

	@Override
	public User createUser(User user) {
		em.persist(user);
		return user;
	}

	@Override
	public User createUserFromOpenIdEmail(String email) {
		// In order to create a User from an OpenID Connect login, we require the email to be present in a municipality.
		//
		// An email address can be used for several Employments. For instance, it's possible for a manager to also have
		// non-manager employment - so we create a role of each of the employment.
		//
		final List<Employment> employments = employmentService.findByEmail(email);
		if(employments.isEmpty()) {
			throw new RuntimeException("No employments found");				//TODO: proper exception. Unathorized.
		}

		final List<Role> roles = createRolesFromEmployments(employments);
		final String name = employments.get(0).getName();					//TODO: better approach than grabbing first name?
		final User user = new User(name, email, roles);

		log.info("Persising {} with roles={}", user, roles);
		return createUser(user);
	}

	@Override
	public List<RolePO> getRoles(long userId) {
		final TypedQuery<Role> query = em.createQuery("SELECT r FROM Role r WHERE r.owner.id = :userId", Role.class);
		query.setParameter("userId", userId);
		final List<Role> roles = query.getResultList();

		final List<RolePO> result = roles.stream()
			.map(RolePO::new)
			.collect(Collectors.toList());
		return result;
	}

	@Override
	public void createRole(Role role) {
		em.persist(role);
	}

	@Override
	public Optional<UserSettings> getSettings(long userId) {
		final TypedQuery<UserSettings> query = em.createQuery("SELECT u FROM UserSettings u WHERE u.userId = :userId", UserSettings.class);
		query.setParameter("userId", userId);

		try {
			UserSettings settings = query.getSingleResult();
			return Optional.of(settings);
		}
		catch(NoResultException ex) {
			return Optional.empty();
		}
	}

	@Override
	public UserSettingsPO getSettingsPO(long userId) {
		//TODO: should create-if-not-existing responsibility be here or in the controller?
		final UserSettings settings = getSettings(userId).orElseGet(
			() -> {
				log.info("getSettingsPO: no existing settings, creating new");
				return createUserSettings(new UserSettings(userId));
			}
		);

		return new UserSettingsPO(settings);
	}

	@Override
	public UserSettings createUserSettings(UserSettings userSettings) {
		em.persist(userSettings);
		return userSettings;
	}

	@Override
	public void updateSettings(UserSettingsPO updatedsettings) {
		UserSettings settings = em.find(UserSettings.class, updatedsettings.getId());
		settings.setScope(updatedsettings.getScope());
		settings.setShowResponsible(updatedsettings.isShowResponsible());
		settings.setShowExpandedOrg(updatedsettings.isShowExpandedOrg());
		em.merge(settings);
	}

	private List<Role> createRolesFromEmployments(List<Employment> employments) {
		return employments.stream()
			.map(emp -> {
				final Role role = new Role();

				role.setManager(emp.getEmployedIn().getChildren().equals(emp));
				role.setEmployment(emp);
				role.setName(String.format("%s (%s)", emp.getName(), emp.getEmployedIn().getName()));

				return role;
			})
			.collect(Collectors.toList());
	}
}
