package dk.os2opgavefordeler.model.presentation;

import dk.os2opgavefordeler.model.DistributionRuleFilterName;

/**
 * Presentation class for filter names
 */
public class FilterNamePO {

	private long id;
	private String name;
	private String type;
	private boolean defaultName;

	public FilterNamePO() {}

	/**
	 * Creates a FilterNamePO object with values set from the source filter name object.
	 *
	 * @param source source object
	 */
	public FilterNamePO(DistributionRuleFilterName source) {
		this.id = source.getId();
		this.name = source.getName();
		this.type = source.getType();
		this.defaultName = source.isDefaultName();
	}

	/**
	 * Creates a FilterNamePO with the specified values.
	 *
	 * @param id ID
	 * @param name filter name
	 * @param defaultName whether the filter name is the default name
	 */
	public FilterNamePO(Long id, String name, String type, boolean defaultName) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.defaultName = defaultName;
	}

	/**
	 * Creates a FilterNamePO with the specified values.
	 *
	 * @param id ID
	 * @param name filter name
	 * @param defaultName whether the filter name is the default name
	 */
	public FilterNamePO(String name, String type, boolean defaultName) {
		this.name = name;
		this.type = type;
		this.defaultName = defaultName;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
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

	@Override
	public String toString() {
		return "FilterNamePO{" +
				"id=" + id +
				", name='" + name + '\'' +
				", defaultName=" + defaultName +
				'}';
	}
}
