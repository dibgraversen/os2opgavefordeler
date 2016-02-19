package dk.os2opgavefordeler.model.api;

import dk.os2opgavefordeler.model.Employment;

public class EmploymentApiResultPO {
	private String name;
	private String email;
	private String esdhId;
	private String esdhName;
	private String initials;
	private String jobTitle;


	public EmploymentApiResultPO(Employment source) {
		this.name = source.getName();
		this.email = source.getEmail();
		this.esdhId = source.getEsdhId();
		this.esdhName = source.getEsdhLabel();
		this.initials = source.getInitials();
		this.jobTitle = source.getJobTitle();
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

	public String getEsdhName() {
		return esdhName;
	}

	public String getInitials() {
		return initials;
	}

	public String getJobTitle() {
		return jobTitle;
	}
}
