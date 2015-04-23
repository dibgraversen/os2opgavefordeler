package dk.os2opgavefordeler.model.kle;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KleGroup {
	private final List<KleTopic> topics = new ArrayList<>();

	private final String number;
	private final String title;
	private final String description;
	private final Date dateCreated;

	public KleGroup(String number, String title, String description, Date dateCreated) {
		this.number = number;
		this.title = title;
		this.description = description;
		this.dateCreated = dateCreated;
	}

	public KleGroup(String number, String title, String description, Date dateCreated, List<KleTopic> topics) {
		this(number, title, description, dateCreated);
		this.topics.addAll(topics);
	}



	public void addTopic(KleTopic topic) {
		topics.add(topic);
	}

	public ImmutableList<KleTopic> getTopics() {
		return ImmutableList.copyOf(topics);
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
		return String.format("KleGroup<%s,%s> - %d topics", number, title, topics.size());
	}
}
