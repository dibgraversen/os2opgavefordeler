package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Kle;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.presentation.DistributionRulePO;
import dk.os2opgavefordeler.rest.DistributionRuleScope;

import java.util.List;
import java.util.Optional;

public interface DistributionService {
	DistributionRule createDistributionRule(DistributionRule rule);
	DistributionRule merge(DistributionRule rule);

	Optional<DistributionRule> getDistribution(long id);
	List<DistributionRule> getDistributionsAll(long municipalityId);

	List<DistributionRule> getDistributionsForOrg(long orgId, long municipalityId, boolean includeImplicit);

	List<DistributionRulePO> getPoDistributions(OrgUnit orgUnit, DistributionRuleScope scope);

	void buildRulesForMunicipality(long municipalityId);

	DistributionRule findAssigned(Kle kle, Municipality municipality);
	Optional<Employment> findResponsibleEmployee(DistributionRule rule);

	List<DistributionRule> getChildren(Long ruleId);
}
