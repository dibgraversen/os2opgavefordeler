package dk.os2opgavefordeler.distribution;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;

import dk.os2opgavefordeler.model.*;

import java.util.ArrayList;
import java.util.List;

@Repository(forEntity = DistributionRule.class)
public abstract class DistributionRuleRepository extends AbstractEntityRepository<DistributionRule, Long> {

	public abstract Iterable<DistributionRule> findByKleAndMunicipality(Kle kle, Municipality municipality);

	@Query("select rule from DistributionRule rule where rule.assignedOrg in ( ?1 )")
	public abstract List<DistributionRule> findRulesByAssignedOrg(List<OrgUnit> orgUnits);

	@Modifying
	@Query("update DistributionRule rule set rule.responsibleOrg = null where rule.responsibleOrg in ( ?1 )")
	abstract void clearResponsible(List<OrgUnit> orgUnits);

	@Modifying
	@Query("update DistributionRule rule set rule.assignedOrg = null where rule.assignedOrg in ( ?1 )")
	abstract void clearAssignedOrg(List<OrgUnit> orgUnits);

	public void clearOrgs(List<OrgUnit> orgUnits){
		clearResponsible(orgUnits);
		clearAssignedOrg(orgUnits);
	}

	@Modifying
	@Query("update DistributionRule rule set rule.assignedEmp = null where rule.assignedEmp in ( ?1 )")
	abstract void clearAssignedEmployments(List<Long> employments);

	@Modifying
	@Query("update OrgUnit org set org.manager = null where org.manager in ( ?1 )")
	abstract void clearManagers(List<Employment> employments);

	public void clearEmployments(List<Employment> employments){
		List<Long> employmentIds = new ArrayList<>();
		for (Employment employment : employments) {
			employmentIds.add(employment.getId());
		}
		clearAssignedEmployments(employmentIds);
		clearManagers(employments);
	}
}