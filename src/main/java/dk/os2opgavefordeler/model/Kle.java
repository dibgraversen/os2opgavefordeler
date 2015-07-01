package dk.os2opgavefordeler.model;

import com.google.common.collect.ImmutableList;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = Kle.TABLE_NAME)
public class Kle implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String TABLE_NAME = "Kle";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	int id;

	@ManyToOne
	@JoinColumn
	private Kle parent;

	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private final List<Kle> children = new ArrayList<>();


	@Column(nullable = false, updatable = false)
	private final String number;

	@Column(nullable = false)
	private final String title;

	@Column(nullable = false)
	@Lob
	private final String description;

	@Column(nullable = false)
	@Temporal(TemporalType.DATE)
	private final Date dateCreated;



	public Kle() {
		//for JPA
		this.number = null;
		this.title = null;
		this.description = null;
		this.dateCreated = null;
	}

	public Kle(String number, String title, String description, Date dateCreated) {
		this.number = number;
		this.title = title;
		this.description = description;
		this.dateCreated = dateCreated;
	}

	public Kle(String number, String title, String description, Date dateCreated, List<Kle> children) {
		this(number, title, description, dateCreated);
		children.forEach(this::addChild);
	}



	public void addChild(Kle child) {
		child.setParent(this);
		if(!children.contains(child)) {
			children.add(child);
		}
	}

	public ImmutableList<Kle> getChildren() {
		return ImmutableList.copyOf(children);
	}

	public int getId() {
		return id;
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



	protected void setParent(Kle parent) {
		if(this.parent != null && this.parent != parent) {
			this.parent.children.remove(this);
		}
		this.parent = parent;
	}



	@Override
	public String toString() {
		return String.format("Kle<%s,%s>", number, title);
	}
}
