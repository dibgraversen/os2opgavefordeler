package dk.os2opgavefordeler.rest;

public enum DistributionRuleScope {
	RESPONSIBLE, INHERITED, ALL;

	public static DistributionRuleScope fromString(String name) {
		return valueOf(name.toUpperCase());
	}
}
