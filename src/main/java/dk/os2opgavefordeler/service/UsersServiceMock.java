package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.model.presentation.RolePO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hlo@miracle.dk
 */
public class UsersServiceMock {

	public List<RolePO> getRoles(int userId){
		List<RolePO> result = new ArrayList<>();
		result.add(buildRolePO(1, 1, "Henrik(dig)", 1, false, false, false, false));
		result.add(buildRolePO(2, 1, "Admin", 0, false, true, false, false));
		result.add(buildRolePO(3, 1, "Jørgen Jensen", 2, false, false, true, false));
		result.add(buildRolePO(4, 1, "Hans Jørgensen", 3, false, false, false, true));
		return result;
	}

	public void createRole(Role role) {

	}

	protected RolePO buildRolePO(int id, int userId, String name, int employment, boolean manager,
														 boolean admin, boolean mAdmin, boolean sub){
		RolePO result = new RolePO();
		result.setId(id);
		result.setUserId(userId);
		result.setName(name);
		result.setEmployment(employment);
		result.setManager(manager);
		result.setAdmin(admin);
		result.setMunicipalityAdmin(mAdmin);
		result.setSubstitute(sub);
		return result;
	}
}
