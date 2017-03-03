package dk.os2opgavefordeler.model.presentation;

public enum KleAssignmentType {
	INTEREST,PERFORMING;

	public static KleAssignmentType fromString(final String s) {
	    return KleAssignmentType.valueOf(s.toUpperCase());
	}
}
