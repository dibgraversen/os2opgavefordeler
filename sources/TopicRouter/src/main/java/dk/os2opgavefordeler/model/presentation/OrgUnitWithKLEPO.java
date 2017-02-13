package dk.os2opgavefordeler.model.presentation;

import java.util.List;

import dk.os2opgavefordeler.model.Kle;

public class OrgUnitWithKLEPO {
	private long id;
	private String name;
	private String parentName;
	//Lists of kle names only
	private List<String> performingKLE;
	private List<String> interestKLE;
	
	public OrgUnitWithKLEPO() {
	}


	public OrgUnitWithKLEPO(long id, String name,String parentName) {
		this();
		this.id = id;
		this.name = name;	
		this.parentName =parentName;
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

	public List<String> getPerformingKLE() {
		return performingKLE;
	}


	public void setPerformingKLE(List<String> performingKLE) {
		this.performingKLE = performingKLE;
	}


	public List<String> getInterestKLE() {
		return interestKLE;
	}


	public void setInterestKLE(List<String> interestKLE) {
		this.interestKLE = interestKLE;
	}


	public String getParentName() {
		return parentName;
	}


	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	

}
