package dk.os2opgavefordeler.model.kle;

import com.google.common.collect.ImmutableList;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = KleGroup.TABLE_NAME)
public class KleGroup implements Serializable, KleParent {
	private static final long serialVersionUID = 1L;
	public static final String TABLE_NAME = "KleGroup";

	@ManyToOne
	@JoinColumn(name = "parent")
	private KleMainGroup parent;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "parent")
	private final List<KleTopic> topics = new ArrayList<>();

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



	private KleGroup() {
		//for JPA
		this.number = null;
		this.title = null;
		this.description = null;
		this.dateCreated = null;
	}

	public KleGroup(String number, String title, String description, Date dateCreated) {
		this.number = number;
		this.title = title;
		this.description = description;
		this.dateCreated = dateCreated;
	}

	public KleGroup(String number, String title, String description, Date dateCreated, List<KleTopic> topics) {
		this(number, title, description, dateCreated);
		for (KleTopic topic : topics) {
			addTopic(topic);
		}
	}



	public void addTopic(KleTopic topic) {
		topic.setParent(this);
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
		return new Date(dateCreated.getTime());
	}



	protected void setParent(KleMainGroup parent) {
		this.parent = parent;
	}



	@Override
	public String toString() {
		return String.format("KleGroup<%s,%s> - %d topics", number, title, topics.size());
	}
}
