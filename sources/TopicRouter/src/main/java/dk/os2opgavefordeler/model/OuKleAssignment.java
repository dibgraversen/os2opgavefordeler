package dk.os2opgavefordeler.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;

import dk.os2opgavefordeler.model.presentation.KleAssignmentType;

@Entity
@Table(name="ou_kle_mapping")
public class OuKleAssignment {
	
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne
	private OrgUnit ou;
	
	@ManyToOne
	private Kle kle;
	
        @Enumerated(EnumType.STRING)
	@Column(name="assignmentType", nullable = false)
	private KleAssignmentType assignmentType;
	
	public OuKleAssignment() { }

	public OuKleAssignment(OrgUnit ou, Kle kle, KleAssignmentType assignmentType) {
		super();
		this.ou = ou;
		this.kle = kle;
		this.assignmentType = assignmentType;
	}

	public OrgUnit getOu() {
		return ou;
	}

	public void setOu(OrgUnit ou) {
		this.ou = ou;
	}

	public Kle getKle() {
		return kle;
	}

	public void setKle(Kle kle) {
		this.kle = kle;
	}

	public KleAssignmentType getAssignmentType() {
		return assignmentType;
	}

	public void setAssignmentType(KleAssignmentType assignmentType) {
		this.assignmentType = assignmentType;
	}
}
