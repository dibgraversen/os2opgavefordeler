package dk.os2opgavefordeler.model.presentation;

import dk.os2opgavefordeler.model.Employment;

public class EmploymentPO {
	private long id;

	private String name;
	private String email;
	private String phone;
	private String esdhId;
	private String esdhName;
	private String initials;
	private String jobTitle;
	private OrgUnitPO employedIn;
	private boolean subordinate;

	public EmploymentPO(Employment source) {
		this.id = source.getId();
		this.name = source.getName();
		this.email = source.getEmail();
		this.phone = source.getPhone();
		this.esdhId = source.getEsdhId();
		this.esdhName = source.getEsdhLabel();
		this.initials = source.getInitials();
		this.jobTitle = source.getJobTitle();
		this.employedIn = new OrgUnitPO(source.getEmployedIn());
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

	public String getPhone() {
		return phone;
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

	public OrgUnitPO getEmployedIn() {
		return employedIn;
	}

	public void setEmployedIn(OrgUnitPO employedIn) {
		this.employedIn = employedIn;
	}
}
