package dk.os2opgavefordeler.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * A person can have multiple employments in the organization structure, which can result in different titles, email
 * addresses, et cetera - thus we operate on an 'employment' rather than an 'employee'.
 */
@Entity
public class Employment implements Serializable {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private boolean isActive;

//	@ManyToOne
//	private OrgUnit employedIn;		// necessary?


	private String name;
	private String email;
	private String esdhId;
	private String initials;
	private String jobTitle;
//	private String employeeNumber;	// present in SydDjurs - used?

	public Employment() {
	}

	private Employment(Builder builder) {
		this();

		this.isActive = builder.isActive;
		this.name = builder.name;
		this.email = builder.email;
		this.esdhId = builder.esdhId;
		this.initials = builder.initials;
		this.jobTitle = builder.jobTitle;
	}



	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private boolean isActive;
		private String name;
		private String email;
		private String esdhId;
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
		public Builder initials(String initials) {
			this.initials = initials;
			return this;
		}
		public Builder jobTitle(String jobTitle) {
			this.jobTitle = jobTitle;
			return this;
		}
	}

	/*
	final Employment emp =
			Employment.builder()
					.isActive(true)
					.name("Jens Hansen")
					.initials("JH")
					.email("jh@test.dk")
					.esdhId("10243")
					.jobTitle("Clerk")
					.build();
	*/
}
