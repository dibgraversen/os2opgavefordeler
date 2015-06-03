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
	private int id;

	/**
	 * Specifies parent KLE id.
	 */
	private int parent;

	/**
	 * The KLE data associated with this.
	 */
	private KLE kle;

	/**
	 * The organisational unit responsible for handling this topic.
	 */
	private int org;

	/**
	 * Employee responsible for handling this topic.
	 */
	private int employee;

	/**
	 * OrgUnit with responsibility for (ownership of) the topic, who can decide who handles the topic.
	 */
	private int responsible;

	public DistributionRulePO() {
	}

	public DistributionRulePO(DistributionRule source) {
		this.parent = source.getParent().map(rule -> rule.getId()).orElse(0);
		this.id = source.getId();

		this.kle = kleFrom(source.getKle());
		this.responsible = source.getResponsibleOrg().map(OrgUnit::getId).orElse(0);
		this.employee = source.getAssignedEmp();
		this.org = source.getAssignedOrg().map(OrgUnit::getId).orElse(0);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getParent() {
		return parent;
	}

	public void setParent(int parent) {
		this.parent = parent;
	}

	public KLE getKle() {
		return kle;
	}

	public void setKle(KLE kle) {
		this.kle = kle;
	}

	public int getOrg() {
		return org;
	}

	public void setOrg(int org) {
		this.org = org;
	}

	public int getEmployee() {
		return employee;
	}

	public void setEmployee(int employee) {
		this.employee = employee;
	}

	public int getResponsible() {
		return responsible;
	}

	public void setResponsible(int responsible) {
		this.responsible = responsible;
	}



	private static KLE kleFrom(Kle in) {
		KLE kle = new KLE();

		kle.setId(in.getId());
		kle.setNumber(in.getNumber());
		kle.setName(in.getTitle());
		kle.setServiceText(in.getDescription());

		//TODO: need to track type
//		if(in instanceof KleMainGroup) {
//			kle.setType("main");
//		} else if(in instanceof KleGroup) {
//			kle.setType("group");
//		} else if(in instanceof KleTopic) {
//			kle.setType("topic");
//		} else {
		kle.setType("<unknown>");

		return kle;
	}
}
