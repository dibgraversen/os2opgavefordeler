package dk.os2opgavefordeler.model.presentation;

public class OrgUnitListPO {
	private long id;
	private String name;
	private String parentName;
	private boolean klesAssigned;

	public OrgUnitListPO() { }

	public OrgUnitListPO(long id, String name,String parentName, boolean kleAssigned) {
		this();
		this.id = id;
		this.name = name;
		this.parentName = parentName;
		this.klesAssigned = kleAssigned;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public boolean isKlesAssigned() {
		return klesAssigned;
	}

	public void setKlesAssigned(boolean klesAssigned) {
		this.klesAssigned = klesAssigned;
	}

}
