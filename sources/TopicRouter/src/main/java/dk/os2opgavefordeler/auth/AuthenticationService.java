package dk.os2opgavefordeler.auth;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

@RequestScoped
public class AuthenticationService {

    @Inject
    private Instance<HttpServletRequest> request;

    public boolean isAuthenticated() {
        return false;
    }

    public Authentication getAuthentication() {
        return (Authentication) request.get().getSession().getAttribute("foo");
    }

}
