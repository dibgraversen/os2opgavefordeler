package dk.os2opgavefordeler.auth;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class AuthenticationProducer {

    @Inject
    private AuthService authService;

    @Produces
    @Default
    public Authentication produceAuthentication() {
        return authService.getAuthentication();
    }
}
