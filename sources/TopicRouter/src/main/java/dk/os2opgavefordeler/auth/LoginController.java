package dk.os2opgavefordeler.auth;

import dk.os2opgavefordeler.employment.UserRepository;
import dk.os2opgavefordeler.model.User;
import org.slf4j.Logger;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@SessionScoped
public class LoginController implements Serializable {

    private Long currentUserid;

    @Inject
    private Logger logger;

    @Inject
    private UserRepository userRepository;

    public void loginAs(User user) {
        currentUserid = user.getId();
    }

    public void login(String email, String token) {
        logger.info("Login, email: {} token: {}", email,token);
        User byEmail = userRepository.findByEmail(email);
        currentUserid = byEmail.getId();
    }

    public void logout() {

        logger.info("Logout");
        currentUserid = null;
    }

    public boolean isLoggedIn() {
        return currentUserid != null;
    }

    public User getUser() {
        if (currentUserid == null) {
            return null;
        }
        return userRepository.findBy(currentUserid);
    }

    @Produces
    @CurrentUser
    @Named
    public User getCurrentUser() {
        return getUser();
    }

}
