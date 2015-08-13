package dk.os2opgavefordeler.model;

import com.google.common.base.MoreObjects;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A person can have multiple employments in the organization structure, which can result in different titles, email
 * addresses, et cetera - thus we operate on an 'employment' rather than an 'employee'.
 */
@Entity
public class Employment implements Serializable {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private boolean isActive = true;

	@ManyToOne
	private OrgUnit employedIn;

	private String businessKey;
	private String name;
	private String email;
	private String esdhId;
	private String esdhLabel;
	private String phone;
	private String initials;
	private String jobTitle;

	@ManyToOne
	private Municipality municipality;

	public Employment() {
	}

	private Employment(Builder builder) {
		this();

		this.isActive = builder.isActive;
		this.businessKey = builder.businessKey;
		this.name = builder.name;
		this.email = builder.email;
		this.esdhId = builder.esdhId;
		this.esdhLabel = builder.esdhLabel;
		this.phone = builder.phone;
		this.initials = builder.initials;
		this.jobTitle = builder.jobTitle;
		this.municipality = builder.municipality;
	}

	//--------------------------------------------------------------------------
	// Getter/setters
	//--------------------------------------------------------------------------
	public long getId() {
		return id;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

	public OrgUnit getEmployedIn() {
		return employedIn;
	}

	public void setEmployedIn(OrgUnit employedIn) {
//		if(this.employedIn != null && this.employedIn != employedIn) {
//			this.employedIn.getEmployees().remove(this);
//		}
		this.employedIn = employedIn;
	}

	public String getBusinessKey() {
		return businessKey;
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

	public String getEsdhId() {
		return esdhId;
	}

	public void setEsdhId(String esdhId) {
		this.esdhId = esdhId;
	}

	public String getEsdhLabel() {
		return esdhLabel;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getInitials() {
		return initials;
	}

	public void setInitials(String initials) {
		this.initials = initials;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public Municipality getMunicipality() {
		return municipality;
	}

	public void setMunicipality(Municipality municipality) {
		this.municipality = municipality;
	}

	//--------------------------------------------------------------------------
	// Builder
	//--------------------------------------------------------------------------
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private boolean isActive;
		private String businessKey;
		private String name;
		private String email;
		private String esdhId;
		private String esdhLabel;
		private String phone;
		private String initials;
		private String jobTitle;
		private Municipality municipality;

		public Employment build() {
			return new Employment(this);
		}

		public Builder isActive(boolean isActive) {
			this.isActive = isActive;
			return this;
		}
		public Builder businessKey(String businessKey) {
			this.businessKey = businessKey;
			return this;
		}
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		public Builder email(String email) {
			this.email = email;
			return this;
		}
		public Builder esdhId(String esdhId) {
			this.esdhId = esdhId;
			return this;
		}
		public Builder esdhLabel(String esdhLabel) {
			this.esdhLabel = esdhLabel;
			return this;
		}
		public Builder phone(String phone) {
			this.phone = phone;
			return this;
		}
		public Builder initials(String initials) {
			this.initials = initials;
			return this;
		}
		public Builder jobTitle(String jobTitle) {
			this.jobTitle = jobTitle;
			return this;
		}

		public Builder municipality(Municipality municipality){
			this.municipality = municipality;
			return this;
		}
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("id", id)
			.add("name", name)
			.add("esdhId", esdhId)
			.add("email", email)
			.add("municipality", municipality)
			.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final Employment that = (Employment) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(isActive, that.isActive) &&
			Objects.equals(employedIn, that.employedIn) &&
			Objects.equals(businessKey, that.businessKey) &&
			Objects.equals(name, that.name) &&
			Objects.equals(email, that.email) &&
			Objects.equals(esdhId, that.esdhId) &&
			Objects.equals(phone, that.phone) &&
			Objects.equals(initials, that.initials) &&
			Objects.equals(jobTitle, that.jobTitle) &&
			Objects.equals(municipality, that.municipality);
	}

	@Override
	public int hashCode() {
		return java.util.Objects.hash(id, isActive, employedIn, businessKey, name, email, esdhId, phone, initials, jobTitle, municipality);
	}
}
