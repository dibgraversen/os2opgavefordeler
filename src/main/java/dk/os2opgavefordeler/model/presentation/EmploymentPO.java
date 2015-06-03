package dk.os2opgavefordeler.model.presentation;

import dk.os2opgavefordeler.model.Employment;

public class EmploymentPO {
	private int id;

//	private boolean isActive;
	private String name;
	private String email;
	private String esdhId;
	private String initials;
	private String jobTitle;


	public EmploymentPO(Employment source) {
		this.id = source.getId();
//		this.isActive = source.a
		this.name = source.getName();
		this.email = source.getEmail();
		this.esdhId = source.getEsdhId();
		this.initials = source.getInitials();
		this.jobTitle = source.getJobTitle();
	}

	public int getId() {
		return id;
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

	public String getInitials() {
		return initials;
	}

	public String getJobTitle() {
		return jobTitle;
	}
}
