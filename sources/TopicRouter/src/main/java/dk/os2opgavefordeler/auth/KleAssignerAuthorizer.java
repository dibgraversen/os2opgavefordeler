package dk.os2opgavefordeler.auth;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.deltaspike.security.api.authorization.Secures;

/**
 * @author Brian Graversen
 */
@ApplicationScoped
public class KleAssignerAuthorizer {

  @Inject
  private AuthService authService;

  @Secures
  @KleAssignerRequired
  public boolean doKleAssignerCheck() throws Exception {
    return authService.isKleAssigner();
  }
}
