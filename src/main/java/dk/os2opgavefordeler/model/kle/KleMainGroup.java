package dk.os2opgavefordeler.model.kle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.google.common.collect.*;

public class KleMainGroup {
	private final List<KleGroup> groups = new ArrayList<>();

	private final String number;
	private final String title;
	private final String description;
	private final Date dateCreated;

	public KleMainGroup(String number, String title, String description, Date dateCreated) {
		this.number = number;
		this.title = title;
		this.description = description;
		this.dateCreated = dateCreated;
	}

	public KleMainGroup(String number, String title, String description, Date dateCreated, List<KleGroup> groups) {
		this(number, title, description, dateCreated);
		this.groups.addAll(groups);
	}



	public void addGroup(KleGroup group) {
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
		return dateCreated;
	}



	@Override
	public String toString() {
		return String.format("KleMainGroup<%s,%s> - %d subgroups", number, title, groups.size());
	}
}
