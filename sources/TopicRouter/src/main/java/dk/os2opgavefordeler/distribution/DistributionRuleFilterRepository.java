package dk.os2opgavefordeler.distribution;

import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.DistributionRuleFilter;
import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.OrgUnit;
import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

import java.util.List;

/**
 * @author hlo@miracle.dk
 */
@Repository(forEntity = DistributionRuleFilter.class)
@Transactional
public abstract class DistributionRuleFilterRepository extends AbstractEntityRepository<DistributionRuleFilter, Long> {

	@Query("select filter from DistributionRuleFilter filter where filter.distributionRule in ( ?1 )")
	public abstract List<DistributionRuleFilter> findForRules(List<DistributionRule> rules);

	@Query("select filter from DistributionRuleFilter filter where filter.assignedOrg in ( ?1 )")
	public abstract List<DistributionRuleFilter> findByAssignedOrg(List<OrgUnit> orgs);

	@Modifying
	@Query("update DistributionRuleFilter filter set filter.assignedEmp = null where filter.assignedEmp in ( ?1 )")
	public abstract void unsetEmployments(List<Employment> employments);

	@Query("select filter from DistributionRuleFilter filter where filter.assignedEmp is null and filter.assignedOrg is null")
	public abstract List<DistributionRuleFilter> findAbandoned();
}
