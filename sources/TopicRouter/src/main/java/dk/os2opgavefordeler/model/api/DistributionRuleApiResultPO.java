package dk.os2opgavefordeler.model.api;

import dk.os2opgavefordeler.model.Kle;
import dk.os2opgavefordeler.model.OrgUnit;

/**
 * A Presentation Object for the TopicRoute model ready for use in the UI.
 * @author hlo@miracle.dk
 */
public class DistributionRuleApiResultPO {

	/**
	 * The KLE data associated with this.
	 */
	private KleApiResultPO kle;

	/**
	 * The organisational unit responsible for handling this topic.
	 */
	private OrgUnitApiResultPO org;

	/**
	 * Employee responsible for handling this topic.
	 */
	private EmploymentApiResultPO employee;

	public DistributionRuleApiResultPO() {
	}

	public DistributionRuleApiResultPO(Kle kle, OrgUnit assignedOrg, EmploymentApiResultPO manager, EmploymentApiResultPO employee) {
		this.kle = kleFrom(kle);
		this.employee = employee;
		this.org = new OrgUnitApiResultPO(assignedOrg, manager);
	}

	public KleApiResultPO getKle() {
		return kle;
	}

	public OrgUnitApiResultPO getOrg() {
		return org;
	}

	public void setOrg(OrgUnitApiResultPO org) {
		this.org = org;
	}

	public EmploymentApiResultPO getEmployee() {
		return employee;
	}

	public void setEmployee(EmploymentApiResultPO employee) {
		this.employee = employee;
	}

	private static KleApiResultPO kleFrom(Kle in) {
		KleApiResultPO kle = new KleApiResultPO();
		kle.setNumber(in.getNumber());
		kle.setName(in.getTitle());
		return kle;
	}
}
