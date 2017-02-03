package dk.os2opgavefordeler.model.rest;

public class KleExtended {

	private String kleNumber;
	private String assignmentType;

	public KleExtended() {
	}

	public KleExtended(String kleNumber, String assignmentType) {
		this.kleNumber = kleNumber;
		this.assignmentType = assignmentType;
	}

	public String getKleNumber() {
		return kleNumber;
	}

	public void setKleNumber(String kleNumber) {
		this.kleNumber = kleNumber;
	}

	public String getAssignmentType() {
		return assignmentType;
	}

	public void setAssignmentType(String assignmentType) {
		this.assignmentType = assignmentType;
	}

}
