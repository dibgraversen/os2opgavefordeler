package dk.os2opgavefordeler.model.kle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.google.common.collect.*;

import javax.persistence.*;
import javax.persistence.Table;

@Entity
@Table(name = KleMainGroup.TABLE_NAME)
public class KleMainGroup implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String TABLE_NAME = "KleMainGroup";

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "parent")
	private final List<KleGroup> groups = new ArrayList<>();

	@Id
	@Column(nullable = false, updatable = false)
	private final String number;

	@Column(nullable = false)
	private final String title;

	@Column(nullable = false)
	private final String description;

	@Column(nullable = false)
	@Temporal(TemporalType.DATE)
	private final Date dateCreated;



	private KleMainGroup() {
		//for JPA
		this.number = null;
		this.title = null;
		this.description = null;
		this.dateCreated = null;
	}

	public KleMainGroup(String number, String title, String description, Date dateCreated) {
		this.number = number;
		this.title = title;
		this.description = description;
		this.dateCreated = dateCreated;
	}

	public KleMainGroup(String number, String title, String description, Date dateCreated, List<KleGroup> groups) {
		this(number, title, description, dateCreated);
		for (KleGroup group : groups) {
			addGroup(group);
		}
	}



	public void addGroup(KleGroup group) {
		group.setParent(this);
		groups.add(group);
	}

	public ImmutableList<KleGroup> getGroups() {
		return ImmutableList.copyOf(groups);
	}



	public String getNumber() {
		return number;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public Date getDateCreated() {
		return new Date(dateCreated.getTime());
	}



	@Override
	public String toString() {
		return String.format("KleMainGroup<%s,%s> - %d subgroups", number, title, groups.size());
	}
}
