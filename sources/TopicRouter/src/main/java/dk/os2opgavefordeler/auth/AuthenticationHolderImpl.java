package dk.os2opgavefordeler.auth;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

@SessionScoped
public class AuthenticationHolderImpl implements AuthenticationHolder, Serializable {

    private static final long serialVersionUID = -12;

    private String email;
    private String token;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

	@Override
	public String toString() {
		return "AuthenticationHolderImpl{" +
				"email='" + email + '\'' +
				", token='" + token + '\'' +
				'}';
	}
}
