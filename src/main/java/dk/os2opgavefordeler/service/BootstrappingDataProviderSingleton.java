package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.model.presentation.RolePO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

/**
 * This class serves the sole purpose of providing bootstrap data to work on, while in development.
 * @author hlo@miracle.dk
 */
@Singleton
@Startup
public class BootstrappingDataProviderSingleton {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	UsersService usersService;

	@PostConstruct
	public void buildRoles(){
		log.warn("Starting Singleton - loading mock roles");
		UsersServiceMock mock = new UsersServiceMock();
		RolePO rolePO1 = mock.buildRolePO(1, 1, "Henrik(dig)", 1, false, false, false, false);
		Role role = rolePO1.toRole();
		usersService.createRole(role);
		RolePO rolePO2 = mock.buildRolePO(2, 1, "Admin", 0, false, true, false, false);
		usersService.createRole(rolePO2.toRole());
		RolePO rolePO3 = mock.buildRolePO(3, 1, "Jørgen Jensen", 2, false, false, true, false);
		usersService.createRole(rolePO3.toRole());
		RolePO rolePO4 = mock.buildRolePO(4, 1, "Hans Jørgensen", 3, false, false, false, true);
		usersService.createRole(rolePO4.toRole());
	}


}
