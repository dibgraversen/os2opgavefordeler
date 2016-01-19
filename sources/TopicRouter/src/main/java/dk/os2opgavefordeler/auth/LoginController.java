package dk.os2opgavefordeler.auth;

import java.io.Serializable;

public class LoginController implements Serializable {
    private static final long serialVersionUID = 6921127441006421645L;

    /*private ActiveUser activeUser = new ActiveUser("", false);
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

    public ActiveUser getCurrentUser() {
        return getUser();
    }*/


}
