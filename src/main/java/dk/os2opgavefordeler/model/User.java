package dk.os2opgavefordeler.model;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name="TR_User")							// "User" is a reserved word in postgres, so we have to rename.
public class User implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String name;
	private String email;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "owner")
	private List<Role> roles;

	@OneToOne
	private UserSettings settings;

	@ManyToOne(fetch = FetchType.EAGER)
	private Municipality municipality;

	public User() {
	}

	public User(String name, String email, List<Role> roles) {
		this.name = name;
		this.email = email;
		this.roles = roles;

		this.roles.forEach(role -> role.setOwner(this));
	}


	//--------------------------------------------------------------------------
	// Getter/setters
	//--------------------------------------------------------------------------
	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public ImmutableList<Role> getRoles() {
		return ImmutableList.copyOf(roles);
	}

	public Municipality getMunicipality() {
		return municipality;
	}

	public void setMunicipality(Municipality municipality) {
		this.municipality = municipality;
	}

	//--------------------------------------------------------------------------
	// toString, equals, hashcode
	//--------------------------------------------------------------------------
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("id", id)
			.add("name", name)
			.add("email", email)
			.add("municipality", municipality)
			.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		User user = (User) o;
		return java.util.Objects.equals(email, user.email);
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(email);
	}
}
