package dk.os2opgavefordeler.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;

/**
 * @author hlo@miracle.dk
 */
@Entity
public class Role implements Serializable {
	@Id
	private long id;

	@ManyToOne
	private User owner;

	private String name;

	private int employment;

	private boolean manager;

	private boolean admin;

	private boolean municipalityAdmin;

	private boolean substitute;

	public Role() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserId() {
		return owner.getId();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getEmployment() {
		return employment;
	}

	public void setEmployment(int employment) {
		this.employment = employment;
	}

	public boolean isManager() {
		return manager;
	}

	public void setManager(boolean manager) {
		this.manager = manager;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public boolean isMunicipalityAdmin() {
		return municipalityAdmin;
	}

	public void setMunicipalityAdmin(boolean municipalityAdmin) {
		this.municipalityAdmin = municipalityAdmin;
	}

	public boolean isSubstitute() {
		return substitute;
	}

	public void setSubstitute(boolean substitute) {
		this.substitute = substitute;
	}

	@Override
	public String toString() {
		return "Role{" +
				"id=" + id +
				", name='" + name + '\'' +
				", employment=" + employment +
				", manager=" + manager +
				", admin=" + admin +
				", municipalityAdmin=" + municipalityAdmin +
				", substitute=" + substitute +
				'}';
	}
}
