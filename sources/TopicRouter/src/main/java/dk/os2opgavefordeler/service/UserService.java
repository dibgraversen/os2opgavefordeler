package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.model.UserSettings;
import dk.os2opgavefordeler.model.presentation.RolePO;
import dk.os2opgavefordeler.model.presentation.SubstitutePO;
import dk.os2opgavefordeler.model.presentation.UserRolePO;
import dk.os2opgavefordeler.model.presentation.UserSettingsPO;

import java.util.List;
import java.util.Optional;

/**
 * @author hlo@miracle.dk
 */
public interface UserService {
	@Deprecated Optional<User> findById(long userId);
	@Deprecated Optional<User> findByEmail(String email);

	List<UserRolePO> getAllUsers();

	User createUser(User user);
	void removeUser(User user) throws ResourceNotFoundException, AuthorizationException;
	User createOrUpdateUser(User user);

	List<RolePO> getRoles(long userId);
	List<Role> getSubstituteRoles(long userId);
	List<RolePO> getAllRoles();
	Optional<Role> findRoleById(long roleId);

	void createRole(Role role);
	void updateRole(Role role);
	void removeRole(long roleId) throws ResourceNotFoundException, AuthorizationException;

	Optional<UserSettings> getSettings(long userId);
	UserSettingsPO getSettingsPO(long userId);

	UserSettings createUserSettings(UserSettings userSettings);

	void updateSettings(UserSettingsPO settings);

	Role createSubstituteRole(long targetEmploymentId, long roleId) throws ResourceNotFoundException, AuthorizationException;
	List<SubstitutePO> findSubstitutesFor(long roleId) throws ResourceNotFoundException, AuthorizationException;
	boolean isAdmin(String email);
	boolean isAdmin(long userId);
	boolean isManager(long userId);
	boolean isMunicipalityAdmin(long userId);
	boolean isKleAssigner(long id);
}
