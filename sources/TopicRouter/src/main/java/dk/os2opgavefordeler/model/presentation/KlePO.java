package dk.os2opgavefordeler.model.presentation;

import dk.os2opgavefordeler.model.Kle;

import com.google.common.base.MoreObjects;

/**
 * Presentation object for a KLE
 */
public class KlePO {

	private static final String MAIN_KLE_TYPE = "main";
	private static final String GROUP_KLE_TYPE = "group";
	private static final String TOPIC_KLE_TYPE = "topic";
	private static final String UNKNOWN_KLE_TYPE = "<unknown>";

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

	private long municipalityId;

	public KlePO() {
	}

	/**
	 * Constructs a new KlePO presentation object with values set from the specified source KLE
	 *
	 * @param source source KLE object
	 */
	public KlePO(Kle source){
		id = source.getId();
		number = source.getNumber();
		name = source.getTitle();
		type = getTypeFromNumber(source.getNumber());
		serviceText = source.getDescription();

		if (source.getMunicipality() != null) { // municipalities are *only* set on a few specific KLEs
			municipalityId = source.getMunicipality().getId();
		}
	}

	private String getTypeFromNumber(String number){
		if (number != null) {
			String[] parts = number.split("\\.");

			if (parts.length == 1) {
				return MAIN_KLE_TYPE;
			}

			if (parts.length == 2)  {
				return GROUP_KLE_TYPE;
			}

			if (parts.length == 3) {
				return TOPIC_KLE_TYPE;
			}

			return UNKNOWN_KLE_TYPE; // unable to determine type
		}

		return "";
	}

	/**
	 * Returns this KlePO presentation object as a Kle object instead
	 *
	 * @return kle object with values set from the presentation object
	 */
	public Kle asKle(){
		Kle result = new Kle();

		result.setId(id);
		result.setNumber(number);
		result.setTitle(name);
		result.setDescription(serviceText);

		return result;
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

	public long getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(long municipalityId) {
		this.municipalityId = municipalityId;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("id", id)
				.add("number", number)
				.add("name", name)
				.add("type", type)
				.add("serviceText", serviceText)
				.add("municipalityId", municipalityId)
				.toString();
	}
}
