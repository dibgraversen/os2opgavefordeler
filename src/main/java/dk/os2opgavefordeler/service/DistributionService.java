package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.presentation.DistributionRulePO;
import dk.os2opgavefordeler.rest.DistributionRuleScope;

import java.util.List;
import java.util.Optional;

public interface DistributionService {
	DistributionRule createDistributionRule(DistributionRule rule);
	DistributionRule merge(DistributionRule rule);

	Optional<DistributionRule> getDistribution(long id);
	List<DistributionRule> getDistributionsAll();

	List<DistributionRule> getDistributionsForOrg(long orgId, boolean includeUnassigned);
	List<DistributionRule> getDistributionsForOrg(long orgId, boolean includeUnassigned, boolean includeImplicit);

	List<DistributionRulePO> getPoDistributions(OrgUnit orgUnit, DistributionRuleScope scope);
}
