package dk.os2opgavefordeler.model.rest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Convenience class for an organisational unit result from an Rest call
 */
public class OURestResultPO {

	private long id;
	private long parentId;
	private String name;
	private List<KleExtended> kles;

	public OURestResultPO() {
		this.kles = new ArrayList<>();
	}

	public OURestResultPO(long id, long parentId, String name, List<KleExtended> kles) {
		this();
		this.id = id;
		this.parentId = parentId;
		this.name = name;
		this.kles = kles;
	}

	public OURestResultPO(long id, long parentId, String name) {
		this();
		this.id = id;
		this.parentId = parentId;
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getParentId() {
		return parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<KleExtended> getKles() {
		return kles;
	}

	public void setKles(List<KleExtended> kles) {
		this.kles = kles;
	}

	public void addKle(String kleNumber, String assignmentType) {
		this.kles.add(new KleExtended(kleNumber,assignmentType));
	}

	public void addKle(KleExtended kleExt) {
		this.kles.add(kleExt);
	}
	
	public void removeKle(String number){		
		this.kles.removeIf(x -> x.getKleNumber().equals(number));
//		for (Iterator<KleExtended> iterator = kles.iterator(); iterator.hasNext();) {
//			KleExtended kleExt = iterator.next();
//		    if(kleExt.getKleNumber().equals(number)){		        
//		        iterator.remove();
//		        return;
//		    }
//		}
	}

}
