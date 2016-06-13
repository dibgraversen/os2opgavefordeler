package dk.os2opgavefordeler.model.presentation;

import dk.os2opgavefordeler.model.DistributionRuleFilterName;

/**
 * Presentation class for filter names
 */
public class FilterNamePO {

	private long id;
	private String name;
	private boolean defaultName;

	/**
	 * Creates a FilterNamePO object with values set from the source filter name object.
	 *
	 * @param source source object
	 */
	public FilterNamePO(DistributionRuleFilterName source) {
		this.id = source.getId();
		this.name = source.getName();
		this.defaultName = source.isDefaultName();
	}

	/**
	 * Creates a FilterNamePO with the specified values.
	 *
	 * @param id ID
	 * @param name filter name
	 * @param defaultName whether the filter name is the default name
	 */
	public FilterNamePO(Long id, String name, boolean defaultName) {
		this.id = id;
		this.name = name;
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
