package dk.os2opgavefordeler.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.persistence.*;
import java.io.Serializable;

/**
 * A person can have multiple employments in the organization structure, which can result in different titles, email
 * addresses, et cetera - thus we operate on an 'employment' rather than an 'employee'.
 */
@Entity
public class Employment implements Serializable {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private boolean isActive;

	@ManyToOne
	private OrgUnit employedIn;		// necessary?

	private String name;
	private String email;
	private String esdhId;
	private String esdhLabel;
	private String phone;
	private String initials;
	private String jobTitle;

	public Employment() {
	}

	private Employment(Builder builder) {
		this();

		this.isActive = builder.isActive;
		this.name = builder.name;
		this.email = builder.email;
		this.esdhId = builder.esdhId;
		this.esdhLabel = builder.esdhLabel;
		this.phone = builder.phone;
		this.initials = builder.initials;
		this.jobTitle = builder.jobTitle;
	}

	//--------------------------------------------------------------------------
	// Getter/setters
	//--------------------------------------------------------------------------
	public int getId() {
		return id;
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

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getEsdhId() {
		return esdhId;
	}

	public String getEsdhLabel() {
		return esdhLabel;
	}

	public String getPhone() {
		return phone;
	}

	public String getInitials() {
		return initials;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	//--------------------------------------------------------------------------
	// Builder
	//--------------------------------------------------------------------------
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private boolean isActive;
		private String name;
		private String email;
		private String esdhId;
		private String esdhLabel;
		private String phone;
		private String initials;
		private String jobTitle;

		public Employment build() {
			return new Employment(this);
		}

		public Builder isActive(boolean isActive) {
			this.isActive = isActive;
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
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("id", id)
			.add("name", name)
			.add("esdh", esdhLabel)
			.toString();
	}
}
