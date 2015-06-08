package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.presentation.DistributionRulePO;

import java.util.List;
import java.util.Optional;

public interface DistributionService {
	DistributionRule createDistributionRule(DistributionRule rule);
	DistributionRule merge(DistributionRule rule);

	Optional<DistributionRule> getDistribution(int id);
	List<DistributionRule> getDistributionsAll();

	List<DistributionRule> getDistributionsForOrg(int orgId, boolean includeUnassigned);
	List<DistributionRule> getDistributionsForOrg(int orgId, boolean includeUnassigned, boolean includeImplicit);

	List<DistributionRulePO> getPoDistributionsAll();
	List<DistributionRulePO> getPoDistributions(int orgId, boolean includeUnassigned, boolean includeImplicit);
}
