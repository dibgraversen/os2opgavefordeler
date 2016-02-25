package dk.os2opgavefordeler.auth;

/**
 * Holds authentication info.
 * Uses the http session object by default.
 *
 * This class is necessary since we don't have a httprequest in unit tests.
 */
public interface AuthenticationHolder {

    String getEmail();

    void setEmail(String email);

    String getToken();

    void setToken(String token);

}
