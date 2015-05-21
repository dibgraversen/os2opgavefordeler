package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.DistributionRule;

import java.util.List;

public interface DistributionService {
	DistributionRule createDistributionRule(DistributionRule rule);
	List<DistributionRule> getDistributionsForOrg(final long orgId, final boolean includeUnassigned);
}
