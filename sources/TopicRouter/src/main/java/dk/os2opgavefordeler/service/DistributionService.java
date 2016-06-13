package dk.os2opgavefordeler.service;

import java.util.List;
import java.util.Optional;

import dk.os2opgavefordeler.model.*;
import dk.os2opgavefordeler.model.presentation.DistributionRulePO;

import dk.os2opgavefordeler.model.presentation.FilterNamePO;
import dk.os2opgavefordeler.rest.DistributionRuleScope;

public interface DistributionService {
	DistributionRule createDistributionRule(DistributionRule rule);

	Optional<DistributionRule> getDistribution(long id);
	List<DistributionRule> getDistributionsAll(long municipalityId);

	List<FilterNamePO> getFilterNamesAll(long municipalityId);
	List<FilterNamePO> getFilterNamesDate(long municipalityId);
	List<FilterNamePO> getFilterNamesText(long municipalityId);
	FilterNamePO getDefaultTextFilterName(long municipalityId);
	void setDefaultTextFilterName(long municipalityId, long filterId);
	FilterNamePO getDefaultDateFilterName(long municipalityId);
	void setDefaultDateFilterName(long municipalityId, long filterId);
	FilterNamePO updateFilterName(long municipalityId, FilterNamePO filterNamePO);
	void deleteFilterName(long municipalityId, long filterId);

	List<DistributionRule> getDistributionsForOrg(long orgId, long municipalityId, boolean includeImplicit);

	List<DistributionRulePO> getPoDistributions(OrgUnit orgUnit, DistributionRuleScope scope);

	void buildRulesForMunicipality(long municipalityId);

	DistributionRule findAssigned(Kle kle, Municipality municipality);
	Optional<Employment> findResponsibleEmployee(DistributionRule rule);

	List<DistributionRule> getChildren(Long ruleId, OrgUnit orgUnit, DistributionRuleScope scope);

	DistributionRule createDistributionRule(Kle kle);
}