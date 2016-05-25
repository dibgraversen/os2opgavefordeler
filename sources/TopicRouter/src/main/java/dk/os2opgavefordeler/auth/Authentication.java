package dk.os2opgavefordeler.auth;

import com.google.common.base.Objects;

/**
 * Convenience class for handling authentication
 */
public class Authentication {

    private String email;

    private String token;

    public Authentication() {
    }

	/**
	 * Creates an Authentication object with the specified email address
	 *
	 * @param email email address
	 */
    public Authentication(String email) {
        this.email = email;
    }

	/**
	 * Creates an Authentication object with the specified email address and login token
	 *
	 * @param email email address
	 * @param token login token
	 */
    public Authentication(String email, String token) {
        this.email = email;
	    this.token = token;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Authentication that = (Authentication) o;
        return Objects.equal(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(email);
    }

    @Override
    public String toString() {
        return "Authentication{" +
                "email='" + email + '\'' +
                '}';
    }
}
