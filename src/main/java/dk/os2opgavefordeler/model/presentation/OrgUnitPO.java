package dk.os2opgavefordeler.model.presentation;

import dk.os2opgavefordeler.model.OrgUnit;

public class OrgUnitPO {
	private int id;
	private int parentId;
	private int managerId;

	private String name;
	private String esdhId;
	private String email;
	private String phone;

	public OrgUnitPO(OrgUnit from) {
		this.id = from.getId();
		this.parentId = from.getParent().map(OrgUnit::getId).orElse(-1);
		this.managerId = from.getManager();

		this.name = from.getName();
		this.esdhId = from.getEsdhId();
		this.email = from.getEmail();
		this.phone = from.getPhone();
	}

	public int getParentId() {
		return parentId;
	}

	public int getManagerId() {
		return managerId;
	}

	public String getName() {
		return name;
	}

	public String getEsdhId() {
		return esdhId;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public int getId() {
		return id;
	}
}
