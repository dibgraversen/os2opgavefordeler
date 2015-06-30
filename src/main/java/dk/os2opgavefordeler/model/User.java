package dk.os2opgavefordeler.model;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name="User_")							// "User" is a reserved word in postgres, so we have to rename.
public class User implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private String email;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Role> roles;

	@OneToOne
	private UserSettings settings;

	public User() {
	}

	public User(String email, List<Role> roles) {
		this.email = email;
		this.roles = roles;
	}


	//--------------------------------------------------------------------------
	// Getter/setters
	//--------------------------------------------------------------------------
	public int getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public ImmutableList<Role> getRoles() {
		return ImmutableList.copyOf(roles);
	}


	//--------------------------------------------------------------------------
	// toString, equals, hashcode
	//--------------------------------------------------------------------------
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("id", id)
			.add("email", email)
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
