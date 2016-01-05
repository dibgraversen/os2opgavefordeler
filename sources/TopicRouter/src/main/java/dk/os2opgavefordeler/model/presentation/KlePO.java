package dk.os2opgavefordeler.model.presentation;

import dk.os2opgavefordeler.model.Kle;

/**
 * @author hlo@miracle.dk
 */
public class KlePO {
	public static final String MAIN = "MAIN";
	public static final String GROUP = "GROUP";
	public static final String TOPIC = "TOPIC";

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

	public KlePO(Kle source){
		id = source.getId();
		number = source.getNumber();
		name = source.getTitle();
		type = getTypeFromNumber(source.getNumber());
		serviceText = source.getDescription();
		municipalityId = source.getMunicipality().getId();
	}

	private String getTypeFromNumber(String number){
		if(number != null){
			String[] parts = number.split("\\.");
			if(parts.length == 1){ return MAIN; }
			if(parts.length == 2){ return GROUP; }
			if(parts.length == 3){ return TOPIC; }
		}
		return "";
	}

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
}
