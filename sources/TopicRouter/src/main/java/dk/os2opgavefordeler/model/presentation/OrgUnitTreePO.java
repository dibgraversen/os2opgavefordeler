package dk.os2opgavefordeler.model.presentation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import dk.os2opgavefordeler.model.OrgUnit;

public class OrgUnitTreePO implements Serializable{
	private static final long serialVersionUID = 3159231060766644353L;
	private long id;
	private String name;
	private boolean klesAssigned;
	private List<OrgUnitTreePO> children;

	public OrgUnitTreePO(OrgUnit from) {
		children = new ArrayList<>();
		this.name = from.getName();
		this.id = from.getId();
		this.klesAssigned = from.hasKles();
		for (OrgUnit child :from.getChildren()) {
			this.children.add(new OrgUnitTreePO(child));
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<OrgUnitTreePO> getChildren() {
		return children;
	}
	
	public void setChildren(List<OrgUnitTreePO> children) {
		this.children = children;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isKlesAssigned() {
		return klesAssigned;
	}

	public void setKlesAssigned(boolean klesAssigned) {
		this.klesAssigned = klesAssigned;
	}
}
