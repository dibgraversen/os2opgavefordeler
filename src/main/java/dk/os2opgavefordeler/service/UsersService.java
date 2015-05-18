package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.model.presentation.RolePO;

import java.util.List;

/**
 * @author hlo@miracle.dk
 */
public interface UsersService {
	List<RolePO> getRoles(long userId);

	void createRole(Role role);
}
