package dk.os2opgavefordeler.auth;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Default;

@RequestScoped
@Alternative
@Default
public class TestAuthenticationHolder implements AuthenticationHolder {

    private String email;
    private String token;

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }
}
