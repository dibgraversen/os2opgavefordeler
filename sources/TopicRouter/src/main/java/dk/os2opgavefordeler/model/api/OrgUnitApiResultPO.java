package dk.os2opgavefordeler.model.api;

import dk.os2opgavefordeler.model.OrgUnit;

public class OrgUnitApiResultPO {
	private EmploymentApiResultPO manager;

	private String name;
	private String esdhId;
	private String esdhName;
	private String email;
	private String phone;

	public OrgUnitApiResultPO(OrgUnit from, EmploymentApiResultPO manager) {
		this.manager = manager;
		this.name = from.getName();
		this.esdhId = from.getEsdhId();
		this.esdhName = from.getEsdhLabel();
		this.email = from.getEmail();
		this.phone = from.getPhone();
	}

	public EmploymentApiResultPO getManager() {
		return manager;
	}

	public String getName() {
		return name;
	}

	public String getEsdhId() {
		return esdhId;
	}

	public String getEsdhName() {
		return esdhName;
	}

	public void setEsdhName(String esdhName) {
		this.esdhName = esdhName;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

}
