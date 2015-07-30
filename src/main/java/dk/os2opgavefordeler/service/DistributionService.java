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

	Optional<DistributionRule> getDistribution(int id);
	List<DistributionRule> getDistributionsAll(int municipalityId);

	List<DistributionRule> getDistributionsForOrg(int orgId, int municipalityId, boolean includeUnassigned);
	List<DistributionRule> getDistributionsForOrg(int orgId, int municipalityId, boolean includeUnassigned, boolean includeImplicit);

	List<DistributionRulePO> getPoDistributions(OrgUnit orgUnit, DistributionRuleScope scope);
}
