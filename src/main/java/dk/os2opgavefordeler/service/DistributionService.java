package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.presentation.DistributionRulePO;

import java.util.List;

public interface DistributionService {
	DistributionRule createDistributionRule(DistributionRule rule);

	List<DistributionRule> getDistributionsAll();
	List<DistributionRule> getDistributionsForOrg(int orgId, boolean includeUnassigned);
	List<DistributionRule> getDistributionsForOrg(int orgId, boolean includeUnassigned, boolean includeImplicit);

	List<DistributionRulePO> getPoDistributionsAll();
	List<DistributionRulePO> getPoDistributions(int orgId, boolean includeUnassigned, boolean includeImplicit);
}
