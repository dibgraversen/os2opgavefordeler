package dk.os2opgavefordeler.service.oidc;

import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import dk.os2opgavefordeler.model.IdentityProvider;
import dk.os2opgavefordeler.service.AuthenticationException;

import java.net.URI;

public interface OpenIdConnect {
	URI beginAuthenticationFlow(IdentityProvider idp, String token, String callbackUrl)
	throws AuthenticationException;

	String finalizeAuthenticationFlow(IdentityProvider idp, String token, String callbackUrl, URI requestUri)
	throws AuthenticationException;
}
