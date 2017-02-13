package dk.os2opgavefordeler.model.presentation;

public class KleExtended {

	private String kleNumber;
	private KleAssignmentType assignmentType;

	public KleExtended() {
	}

	public KleExtended(String kleNumber, KleAssignmentType assignmentType) {
		this.kleNumber = kleNumber;
		this.assignmentType = assignmentType;
	}

	public String getKleNumber() {
		return kleNumber;
	}

	public void setKleNumber(String kleNumber) {
		this.kleNumber = kleNumber;
	}

	public KleAssignmentType getAssignmentType() {
		return assignmentType;
	}

	public void setAssignmentType(KleAssignmentType assignmentType) {
		this.assignmentType = assignmentType;
	}

}
