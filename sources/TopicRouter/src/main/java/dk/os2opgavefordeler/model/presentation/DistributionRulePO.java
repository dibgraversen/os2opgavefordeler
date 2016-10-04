package dk.os2opgavefordeler.model.presentation;

import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.OrgUnit;

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
	private KlePO kle;

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

	// Hack, sorry
	private String responsibleOrgName;
	private String responsibleManagerName;

	public DistributionRulePO() {
	}

	public DistributionRulePO(DistributionRule source) {
		this.parent = source.getParent().map(rule -> rule.getId()).orElse(0L);
		this.id = source.getId();
		this.kle = new KlePO(source.getKle());
		this.responsible = source.getResponsibleOrg().map(OrgUnit::getId).orElse(0L);

		if (source.getResponsibleOrg().isPresent()) {
			this.responsibleOrgName = source.getResponsibleOrg().get().getName();

			if (source.getResponsibleOrg().get().getManager().isPresent()) {
				this.responsibleManagerName = source.getResponsibleOrg().get().getManager().get().getName();
			}
		}

		this.employee = source.getAssignedEmp();
		this.org = source.getAssignedOrg().map(OrgUnit::getId).orElse(0L);
	}

	public String getResponsibleOrgName(){
		return responsibleOrgName;
	}

	public void setResponsibleOrgName(String r){
		this.responsibleOrgName = r;
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

	public KlePO getKle() {
		return kle;
	}

	public void setKle(KlePO kle) {
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

	@Override
	public String toString() {
		return "DistributionRulePO{" +
				"id=" + id +
				", parent=" + parent +
				", kle=" + kle +
				", org=" + org +
				", employee=" + employee +
				", responsible=" + responsible +
				", responsibleOrgName='" + responsibleOrgName + '\'' +
				", responsibleManagerName='" + responsibleManagerName + '\'' +
				'}';
	}
}
