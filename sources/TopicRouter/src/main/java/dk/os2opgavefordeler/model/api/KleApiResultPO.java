package dk.os2opgavefordeler.model.api;

/**
 * Convenience class for a KLE result from an API call
 *
 * @author hlo@miracle.dk
 */
public class KleApiResultPO {

	/**
	 * KLE number e.g. 01.06.01.
	 */
	private String number;

	/**
	 * KLE name.
	 */
	private String name;

	public KleApiResultPO() {
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
}
