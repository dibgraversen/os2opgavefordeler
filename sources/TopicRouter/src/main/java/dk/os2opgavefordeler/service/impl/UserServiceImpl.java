package dk.os2opgavefordeler.service.impl;

import dk.os2opgavefordeler.auth.openid.OpenIdUserFactory;

import dk.os2opgavefordeler.repository.EmploymentRepository;
import dk.os2opgavefordeler.repository.UserRepository;

import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.model.UserSettings;
import dk.os2opgavefordeler.model.presentation.RolePO;
import dk.os2opgavefordeler.model.presentation.SubstitutePO;
import dk.os2opgavefordeler.model.presentation.UserRolePO;
import dk.os2opgavefordeler.model.presentation.UserSettingsPO;

import dk.os2opgavefordeler.service.*;

import org.apache.deltaspike.jpa.api.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

import javax.inject.Inject;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class UserServiceImpl implements UserService {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	private EntityManager em;

	@Inject
	private EmploymentService employmentService;

	@Inject
	private EmploymentRepository employmentRepository;

	@Inject
	private UserRepository userRepo;

	@Inject
	private OpenIdUserFactory openIdUserFactory;

	@Inject
	private AuthorizationService auth;

	@Inject
	private ConfigService configService;

	private static Optional<Role> hasRoleFor(User user, long employmentId) {
		return user.getRoles().stream()
				.filter(role -> role.getEmployment().map(
						emp -> (emp.getId() == employmentId)
						).orElse(false)
				).findFirst();
	}

	@Override
	public Optional<User> findById(long userId) {
		TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.id = :userId", User.class);
		query.setParameter("userId", userId);

		try {
			return Optional.of(query.getSingleResult());
		} catch (NoResultException ex) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<User> findByEmail(String email) {
		TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
		query.setParameter("email", email);

		try {
			return Optional.of(query.getSingleResult());
		} catch (NoResultException ex) {
			return Optional.empty();
		}
	}

	@Override
	public List<UserRolePO> getAllUsers() {
		List<UserRolePO> results = new ArrayList<>();

		TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);

		for (User currUser : query.getResultList()) {
			// get non-substitute role
			Role assignedRole = null;

			for (Role currRole : currUser.getRoles()) {
				if (!currRole.isSubstitute()) {
					assignedRole = currRole;
				}
			}

			if (assignedRole != null) {
				results.add(new UserRolePO(currUser, assignedRole));
			} else {
				results.add(new UserRolePO(currUser, null));
			}
		}

		return results;
	}

	@Override
	public User createUser(User user) {
		return userRepo.save(user);
	}

	@Override
	public User createOrUpdateUser(User user) {
		return userRepo.saveAndFlushAndRefresh(user);
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
	public List<Role> getSubstituteRoles(long userId) {
		List<Role> substituteRoles = new ArrayList<>();

		try {
			// get the actual non-substitute role
			final TypedQuery<Role> mainRoleQuery = em.createQuery("SELECT r FROM Role r WHERE r.owner.id = :userId and r.substitute = false", Role.class);
			mainRoleQuery.setParameter("userId", userId);

			final Role mainRole = mainRoleQuery.getSingleResult();

			Optional<Employment> employment = mainRole.getEmployment();

			if (employment.isPresent()) {
				long employmentId = employment.get().getId();

				final TypedQuery<Role> substituteRolesQuery = em.createQuery("SELECT r FROM Role r WHERE r.employment.id = :employmentId and r.substitute = true", Role.class);
				substituteRolesQuery.setParameter("employmentId", employmentId);

				substituteRoles = substituteRolesQuery.getResultList();
			}

		} catch (NoResultException e) {
			log.info("No substitute roles found for user with ID: {}", userId);
		}

		return substituteRoles;
	}

	@Override
	public List<RolePO> getAllRoles() {
		final TypedQuery<Role> query = em.createQuery("SELECT r FROM Role r", Role.class);

		final List<Role> roles = query.getResultList();

		final List<RolePO> result = roles.stream()
				.map(RolePO::new)
				.collect(Collectors.toList());
		return result;
	}

	@Override
	public Optional<Role> findRoleById(long roleId) {
		TypedQuery<Role> query = em.createQuery("SELECT r FROM Role r WHERE r.id = :roleId", Role.class);
		query.setParameter("roleId", roleId);

		try {
			return Optional.of(query.getSingleResult());
		} catch (NoResultException ex) {
			return Optional.empty();
		}
	}

	@Override
	public void createRole(Role role) {
		em.persist(role);
	}

	@Override
	public void updateRole(Role role) {
		em.merge(role);
	}

	@Override
	public void removeRole(long roleId) throws ResourceNotFoundException, AuthorizationException {
		final Role role = findRoleById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role not found"));
		auth.verifyCanActAs(role);
		em.remove(role);
	}

	@Override
	public void removeUser(User user) throws ResourceNotFoundException, AuthorizationException {
		// remove substitute roles first
		for (Role currSubstituteRole : getSubstituteRoles(user.getId())) {
			removeRole(currSubstituteRole.getId());
		}

		// then remove main roles
		for (Role currRole : user.getRoles()) {
			removeRole(currRole.getId());
			user.removeRole(currRole);
		}

		// lastly, remove the user itself
		userRepo.removeAndFlush(user);
	}

	@Override
	public Optional<UserSettings> getSettings(long userId) {
		final TypedQuery<UserSettings> query = em.createQuery("SELECT u FROM UserSettings u WHERE u.userId = :userId", UserSettings.class);
		query.setParameter("userId", userId);

		try {
			UserSettings settings = query.getSingleResult();
			return Optional.of(settings);
		} catch (NoResultException ex) {
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
	public void updateSettings(UserSettingsPO updatedSettings) {
		UserSettings settings = em.find(UserSettings.class, updatedSettings.getId());
		settings.setScope(updatedSettings.getScope());
		settings.setShowResponsible(updatedSettings.isShowResponsible());
		settings.setShowExpandedOrg(updatedSettings.isShowExpandedOrg());
		em.merge(settings);
	}

	public Role createSubstituteRole(long targetEmploymentId, long roleId)
			throws ResourceNotFoundException, AuthorizationException {
		auth.verifyIsAdmin();
		final Employment targetEmployment = employmentService.getEmployment(targetEmploymentId)
				.orElseThrow(() -> new ResourceNotFoundException("Employment not found"));
		final User targetUser = findByEmail(targetEmployment.getEmail())
				.orElseGet(() -> openIdUserFactory.createUserFromOpenIdEmail(targetEmployment.getEmail()));
		final Role sourceRole = findRoleById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role not found"));
		auth.verifyCanActAs(sourceRole);

		final Employment employment = sourceRole.getEmployment().orElseThrow(() -> new ResourceNotFoundException("Role has no employment"));

		Optional<Role> existingRole = hasRoleFor(targetUser, employment.getId());
		if (existingRole.isPresent()) {
			log.warn("createSubstituteRole: {} already has substitute role for {}", targetUser, employment);
			//Don't add role if it already exists - and there's no reason to throw a hissy fit about it.
			return existingRole.get();
		}
		final Role substituteRole = Role.builder()
				.name(sourceRole.getName())
				.substitute(true)
				.manager(sourceRole.isManager())
				.employment(employment)
				.build();
		targetUser.addRole(substituteRole);
		userRepo.saveAndFlush(targetUser);
		return substituteRole;
	}

	@Override
	public List<SubstitutePO> findSubstitutesFor(long roleId) throws ResourceNotFoundException, AuthorizationException {
		final Role role = findRoleById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role not found"));
		auth.verifyCanActAs(role);

		if (!role.getEmployment().isPresent()) {
			log.info("findSubstitutesFor {}: has no employment", role);
			return Collections.emptyList();
		}

		final TypedQuery<Role> query = em.createQuery("SELECT r FROM Role r WHERE r.employment = :emp AND r.substitute = true", Role.class);
		query.setParameter("emp", role.getEmployment().get());

		return query.getResultList().stream()
				.peek(r -> log.info("findSubstitutesFor role {} - owner {}", r, r.getOwner()))
				.map(r -> new SubstitutePO(r.getOwner().getName(), r.getId()))
				.collect(Collectors.toList());
	}

	@Override
	public boolean isAdmin(String email) {
		Optional<User> user = findByEmail(email);

		if (user.isPresent()) {
			return isAdmin(user.get().getId());
		}

		return false;
	}

	@Override
	public boolean isAdmin(long userId) {
		List<RolePO> roles = getRoles(userId);

		Iterator<RolePO> roleIterator = roles.iterator();

		boolean isAdmin = false;

		while (roleIterator.hasNext() && !isAdmin) {
			isAdmin = roleIterator.next().isAdmin();
		}

		return isAdmin;
	}

	@Override
	public boolean isManager(long userId) {
		List<RolePO> roles = getRoles(userId);

		Iterator<RolePO> roleIterator = roles.iterator();

		boolean isManager = false;

		while (roleIterator.hasNext() && !isManager) {
			isManager = roleIterator.next().isManager();
		}

		return isManager;
	}

	@Override
	public boolean isMunicipalityAdmin(long userId) {
		List<RolePO> roles = getRoles(userId);

		Iterator<RolePO> roleIterator = roles.iterator();

		boolean isMunicipalityAdmin = false;

		while (roleIterator.hasNext() && !isMunicipalityAdmin) {
			isMunicipalityAdmin = roleIterator.next().isMunicipalityAdmin();
		}

		return isMunicipalityAdmin;
	}
	
	@Override
	public boolean isKleAssigner(long userId) {
		List<RolePO> roles = getRoles(userId);

		Iterator<RolePO> roleIterator = roles.iterator();

		boolean isKleAssigner = false;

		while (roleIterator.hasNext() && !isKleAssigner) {
			isKleAssigner = roleIterator.next().isKleAssigner();
		}

		return isKleAssigner;
	}
}
