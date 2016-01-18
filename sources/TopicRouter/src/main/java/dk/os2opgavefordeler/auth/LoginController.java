package dk.os2opgavefordeler.auth;

import dk.os2opgavefordeler.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import java.io.Serializable;

@SessionScoped
public class LoginController implements Serializable {

    private ActiveUser activeUser = new ActiveUser("", false);
    private Logger logger = LoggerFactory.getLogger(LoginController.class.getName());

    public void loginAs(User user) {
        logger.info("Logging in as: {}", user);
        activeUser = new ActiveUser(user.getEmail(), true);
    }

    public void login(String email) {
        logger.info("Login, email: {}", email);
        activeUser = new ActiveUser(email, true);
    }

    public void logout() {
        logger.info("Logout");
        activeUser = new ActiveUser("", false);
    }

    public boolean isLoggedIn() {
        return activeUser.isLoggedIn();
    }

    public ActiveUser getUser() {
        return activeUser;
    }

    @Produces
    @dk.os2opgavefordeler.auth.ActiveUser
    @Named
    public ActiveUser getCurrentUser() {
        return getUser();
    }

    public class ActiveUser implements Serializable {
        private String email;
        private boolean isLoggedIn;

        public ActiveUser(String email, boolean isLoggedIn) {

            this.email = email;
            this.isLoggedIn = isLoggedIn;
        }

        public String getEmail() {
            return email;
        }

        public boolean isLoggedIn() {
            return isLoggedIn;
        }
    }

}
