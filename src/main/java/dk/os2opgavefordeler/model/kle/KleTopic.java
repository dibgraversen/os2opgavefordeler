package dk.os2opgavefordeler.model.kle;

import java.util.Date;

public class KleTopic {
	private final String number;
	private final String title;
	private final String description;
	private final Date dateCreated;

	public KleTopic(String number, String title, String description, Date dateCreated) {
		this.number = number;
		this.title = title;
		this.description = description;
		this.dateCreated = dateCreated;
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
		return String.format("KleTopic<%s,%s>", number, title);
	}
}
