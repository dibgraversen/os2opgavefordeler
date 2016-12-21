package dk.os2opgavefordeler.model;

import javax.persistence.*;

@Entity
public class DistributionRuleFilterName {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String type;

	@Column(name = "default_name", nullable = false)
	private boolean defaultName;

	@ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private Municipality municipality;

	public DistributionRuleFilterName() {}

	public DistributionRuleFilterName(String name, String type, boolean defaultName) {
		this.name = name;
		this.type = type;
		this.defaultName = defaultName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isDefaultName() {
		return defaultName;
	}

	public void setDefaultName(boolean defaultName) {
		this.defaultName = defaultName;
	}

	public Municipality getMunicipality() {
		return municipality;
	}

	public void setMunicipality(Municipality municipality) {
		this.municipality = municipality;
	}

	@Override
	public String toString() {
		return "DistributionRuleFilterName{" +
				"id=" + id +
				", name='" + name + '\'' +
				", type='" + type + '\'' +
				", defaultName=" + defaultName +
				", municipality=" + municipality +
				'}';
	}

}
