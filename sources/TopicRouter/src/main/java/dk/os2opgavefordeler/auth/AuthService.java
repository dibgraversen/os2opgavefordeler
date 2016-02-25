package dk.os2opgavefordeler.auth;

import dk.os2opgavefordeler.employment.UserRepository;
import dk.os2opgavefordeler.model.User;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AuthService {

    @Inject
    private AuthenticationHolder authenticationHolder;

    @Inject
    private UserRepository userRepository;

    /**
     *
     * @return true if the user is authenticated.
     */
    public boolean isAuthenticated() {
        return StringUtils.isNotEmpty(authenticationHolder.getEmail());
    }

    /**
     *
     * @return current authentication info.
     */
    public Authentication getAuthentication() {
        return new Authentication(authenticationHolder.getEmail());
    }

    /**
     * Authenticates using email and token. Used as authentication with api access.
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

}
