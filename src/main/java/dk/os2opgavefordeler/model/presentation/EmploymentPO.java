package dk.os2opgavefordeler.model.presentation;

import dk.os2opgavefordeler.model.Employment;

public class EmploymentPO {
	private long id;

//	private boolean isActive;
	private String name;
	private String email;
	private String esdhId;
	private String esdhName;
	private String initials;
	private String jobTitle;
	private boolean subordinate;

	public EmploymentPO(Employment source) {
		this.id = source.getId();
//		this.isActive = source.a
		this.name = source.getName();
		this.email = source.getEmail();
		this.esdhId = source.getEsdhId();
		this.esdhName = source.getEsdhLabel();
		this.initials = source.getInitials();
		this.jobTitle = source.getJobTitle();
	}

	public long getId() {
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

	public String getEsdhName() {
		return esdhName;
	}

	public String getInitials() {
		return initials;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public boolean isSubordinate() {
		return subordinate;
	}

	public void setSubordinate(boolean subordinate) {
		this.subordinate = subordinate;
	}
}
