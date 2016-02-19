package dk.os2opgavefordeler.auth;

import java.io.Serializable;

public class ActiveUser implements Serializable {

    private static final long serialVersionUID = -45304076583325578L;
    private String email;
    private boolean isLoggedIn;

    public ActiveUser(String email, boolean isLoggedIn) {

        this.email = email;
        this.isLoggedIn = isLoggedIn;
    }

    public String getEmail() {
        return email;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }
}