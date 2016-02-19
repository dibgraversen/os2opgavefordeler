package dk.os2opgavefordeler.auth;

import com.google.common.base.Objects;

public class Authentication {

    private String email;

    public Authentication() {
    }

    public Authentication(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
