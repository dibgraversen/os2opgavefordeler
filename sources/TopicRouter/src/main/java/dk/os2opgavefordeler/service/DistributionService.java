package dk.os2opgavefordeler.service;

import java.util.List;
import java.util.Optional;

import dk.os2opgavefordeler.model.*;
import dk.os2opgavefordeler.model.presentation.DistributionRulePO;

import dk.os2opgavefordeler.rest.DistributionRuleScope;

public interface DistributionService {
	DistributionRule createDistributionRule(DistributionRule rule);

	Optional<DistributionRule> getDistribution(long id);
	List<DistributionRule> getDistributionsAll(long municipalityId);

	List<DistributionRuleFilterName> getFilterNamesAll(long municipalityId);
	List<DistributionRuleFilterName> getFilterNamesDate(long municipalityId);
	List<DistributionRuleFilterName> getFilterNamesText(long municipalityId);
	DistributionRuleFilterName getDefaultTextFilterName(long municipalityId);
	void setDefaultTextFilterName(long municipalityId, long filterId);
	DistributionRuleFilterName getDefaultDateFilterName(long municipalityId);
	void setDefaultDateFilterName(long municipalityId, long filterId);
	void deleteFilterName(long municipalityId, long filterId);

	List<DistributionRule> getDistributionsForOrg(long orgId, long municipalityId, boolean includeImplicit);

	List<DistributionRulePO> getPoDistributions(OrgUnit orgUnit, DistributionRuleScope scope);

	void buildRulesForMunicipality(long municipalityId);

	DistributionRule findAssigned(Kle kle, Municipality municipality);
	Optional<Employment> findResponsibleEmployee(DistributionRule rule);

	List<DistributionRule> getChildren(Long ruleId, OrgUnit orgUnit, DistributionRuleScope scope);

	DistributionRule createDistributionRule(Kle kle);
}