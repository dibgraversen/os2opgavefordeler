package dk.os2opgavefordeler.model;

import com.google.common.base.MoreObjects;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * @author hlo@miracle.dk
 */
@Entity
public class Municipality implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private boolean active;

    private String token;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<User> users;

    public Municipality() {

    }

    public Municipality(String name) {
        this.name = name;
        this.token = name;
        this.active = true;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("active", active)
                .toString();
    }
}
