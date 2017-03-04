package dk.os2opgavefordeler.auth;

import javax.enterprise.context.ApplicationScoped;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.NoResultException;

import dk.os2opgavefordeler.LoggedInUser;
import dk.os2opgavefordeler.service.UserService;

import dk.os2opgavefordeler.repository.UserRepository;
import dk.os2opgavefordeler.model.User;
import org.slf4j.Logger;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Authentication service
 */
@ApplicationScoped
public class AuthService {

	@Inject
	private AuthenticationHolder authenticationHolder;

	@Inject
	private UserRepository userRepository;

	@Inject
	private UserService userService;

	@Inject
	Logger logger;

	/**
	 * Checks whether the uses has been authenticated
	 *
	 * @return true if the user has been authenticated.
	 */
	public boolean isAuthenticated() {
		return emailFound();
	}

	/**
	 * Checks wether the user has admin rights.
	 *
	 * @return true if the user has admin rights.
	 */
	public boolean isAdmin() { return userService.isAdmin(currentUser().getEmail()); }

	/**
	 * Checks wether the user has municipality admin rights
	 *
	 * @return true if the user has municipality admin rights
	 */
	public boolean isMunicipalityAdmin() { return userService.isMunicipalityAdmin(currentUser().getId()); }

	/**
	 * Verifies that the user has the KleAssigner role
	 * 
	 * @return true if the user has the KleAssigner role
	 */
	public boolean isKleAssigner() { return userService.isKleAssigner(currentUser().getId()); }
	
	/**
	 * Returns authentication information
	 *
	 * @return current authentication info.
	 */
	public Authentication getAuthentication() {
		return new Authentication(authenticationHolder.getEmail(), authenticationHolder.getToken());
	}

	/**
	 * Authenticates using email and token. Used as authentication with api access.
	 *
	 * @param email of the user.
	 * @param token of the municipality.
	 */
	public void authenticateWithEmailAndToken(String email, String token) {
		authenticationHolder.setEmail(email);
		authenticationHolder.setToken(token);
	}

	/**
	 * Authenticates as the given email.
	 *
	 * @param email to authenticate as.
	 */
	public void authenticateAs(String email) {
		authenticationHolder.setEmail(email);
		User byEmail = userRepository.findByEmail(email);
		authenticationHolder.setToken(byEmail.getMunicipality().getToken());
	}

	/**
	 * Logout the current user.
	 */
	public void logout() {
		authenticationHolder.setEmail(null);
		authenticationHolder.setToken(null);
	}

	/**
	 * Verifies a user is logged in and checks whether given employmentId is in users employments.
	 * @param employmentId Id to check for amongst current users employments.
	 * @return true if user is logged in and has given employment registered.
	 */
	public boolean hasEmployment(Long employmentId){
		return isAuthenticated() && userRepository.hasEmployment(currentUser().getId(), employmentId);

	}

	@Produces @LoggedInUser
	public User currentUser(){
		try	{
			return userRepository.findByEmail(authenticationHolder.getEmail());
		} catch (NoResultException nre){
			if(emailFound()){
				logger.warn("No user found for: {}", authenticationHolder.getEmail());
			}
			return new User();
		}
	}
	
	private boolean emailFound(){
		return isNotEmpty(authenticationHolder.getEmail());
	}
}
