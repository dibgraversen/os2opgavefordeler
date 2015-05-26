package dk.os2opgavefordeler.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * An assignment of a KLE topic to a given organizational unit, optionally to a specific employee.
 *
 * Would 'TopicRoute' be a better name?
 */
@Entity
public class DistributionAssignment implements Serializable {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	long id;

	private boolean isActive;

	@ManyToOne()
	private OrgUnit org;	// non-null

	@ManyToOne
	private Employment employee;

	@Temporal(TemporalType.DATE)
	private Date validFrom;

	@Temporal(TemporalType.DATE)
	private Date validTo;

	public DistributionAssignment() {
	}
}
