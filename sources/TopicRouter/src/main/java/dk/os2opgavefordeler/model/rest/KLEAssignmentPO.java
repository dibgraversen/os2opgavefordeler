package dk.os2opgavefordeler.model.rest;

public class KLEAssignmentPO {
	private String number;
	private String assignmentType;

	public KLEAssignmentPO() {

	}

	public KLEAssignmentPO(String number, String assignmentType) {
		this.number = number;
		this.assignmentType = assignmentType;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getAssignmentType() {
		return assignmentType;
	}

	public void setAssignmentType(String assignmentType) {
		this.assignmentType = assignmentType;
	}

}
