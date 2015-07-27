package dk.os2opgavefordeler.model.presentation;

import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.Kle;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.osto.model.KLE;

/**
 * A Presentation Object for the TopicRoute model ready for use in the UI.
 * @author hlo@miracle.dk
 */
public class DistributionRulePO {

	/**
	 * Id of the topic route for identification.
	 */
	private long id;

	/**
	 * Specifies parent KLE id.
	 */
	private long parent;

	/**
	 * The KLE data associated with this.
	 */
	private KLE kle;

	/**
	 * The organisational unit responsible for handling this topic.
	 */
	private long org;

	/**
	 * Employee responsible for handling this topic.
	 */
	private long employee;

	/**
	 * OrgUnit with responsibility for (ownership of) the topic, who can decide who handles the topic.
	 */
	private long responsible;

	public DistributionRulePO() {
	}

	public DistributionRulePO(DistributionRule source) {
		this.parent = source.getParent().map(rule -> rule.getId()).orElse(0L);
		this.id = source.getId();

		this.kle = kleFrom(source.getKle());
		this.responsible = source.getResponsibleOrg().map(OrgUnit::getId).orElse(0L);
		this.employee = source.getAssignedEmp();
		this.org = source.getAssignedOrg().map(OrgUnit::getId).orElse(0L);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getParent() {
		return parent;
	}

	public void setParent(long parent) {
		this.parent = parent;
	}

	public KLE getKle() {
		return kle;
	}

	public void setKle(KLE kle) {
		this.kle = kle;
	}

	public long getOrg() {
		return org;
	}

	public void setOrg(long org) {
		this.org = org;
	}

	public long getEmployee() {
		return employee;
	}

	public void setEmployee(long employee) {
		this.employee = employee;
	}

	public long getResponsible() {
		return responsible;
	}

	public void setResponsible(long responsible) {
		this.responsible = responsible;
	}



	private static KLE kleFrom(Kle in) {
		KLE kle = new KLE();

		kle.setId(in.getId());
		kle.setNumber(in.getNumber());
		kle.setName(in.getTitle());
		kle.setServiceText(in.getDescription());

		//TODO: need to track type in a cleaner way than this.
		String num = kle.getNumber();
		if(num.length() == 2) {
			kle.setType("main");
		} else if(num.length() == 5) {
			kle.setType("group");
		} else if(num.length() == 8) {
			kle.setType("topic");
		} else {
			kle.setType("<unknown>");
		}

		return kle;
	}
}
