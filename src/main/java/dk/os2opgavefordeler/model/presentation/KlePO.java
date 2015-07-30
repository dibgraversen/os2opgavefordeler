package dk.os2opgavefordeler.model.presentation;

/**
 * @author hlo@miracle.dk
 */
public class KlePO {

	/**
	 * Id for reference.
	 */
	private long id;

	/**
	 * KLE number e.g. 01.06.01.
	 */
	private String number;

	/**
	 * KLE name.
	 */
	private String name;

	/**
	 * The 'type' of KLE. One of main/group/topic
	 */
	private String type;

	/**
	 * Servicetext for the KLE, if present.
	 */
	private String serviceText;

	public KlePO() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
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

	public String getServiceText() {
		return serviceText;
	}

	public void setServiceText(String serviceText) {
		this.serviceText = serviceText;
	}
}
