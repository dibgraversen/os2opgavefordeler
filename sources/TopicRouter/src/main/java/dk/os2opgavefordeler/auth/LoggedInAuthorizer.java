package dk.os2opgavefordeler.auth;

import org.apache.deltaspike.security.api.authorization.Secures;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;

@ApplicationScoped
public class LoggedInAuthorizer {

    /*@Inject
    private LoginController loginController;

    @Secures
    @LoggedInUser
    public boolean doSecuredCheck(InvocationContext invocationContext, BeanManager manager) throws Exception {
        return loginController.isLoggedIn();
    }*/
}