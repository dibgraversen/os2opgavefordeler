package dk.os2opgavefordeler.model.presentation;

import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.OrgUnit;

public class OrgUnitPO {
	private long id;
	private long parentId;
	private long managerId;

	private String name;
	private String esdhId;
	private String email;
	private String phone;
	private Municipality municipality;

	public OrgUnitPO(OrgUnit from) {
		this.id = from.getId();
		this.parentId = from.getParent().map(OrgUnit::getId).orElse(-1L);
		this.managerId = from.getManager().map(Employment::getId).orElse(-1L);

		this.name = from.getName();
		this.esdhId = from.getEsdhId();
		this.email = from.getEmail();
		this.phone = from.getPhone();
		this.municipality = from.getMunicipality().orElse(null);
	}

	public long getParentId() {
		return parentId;
	}

	public long getManagerId() {
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

	public long getId() {
		return id;
	}

	public Municipality getMunicipality() {
		return municipality;
	}

	public void setMunicipality(Municipality municipality) {
		this.municipality = municipality;
	}
}
