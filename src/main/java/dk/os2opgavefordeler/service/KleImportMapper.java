package dk.os2opgavefordeler.service;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import dk.os2opgavefordeler.model.kle.*;
import dk.os2opgavefordeler.model.kle_import.*;

public class KleImportMapper {
	public static List<KleMainGroup> mapMainGroupList(KLEEmneplanKomponent input) {
		return Lists.transform(input.getHovedgruppe(), new Function<HovedgruppeKomponent, KleMainGroup>() {
			public KleMainGroup apply(HovedgruppeKomponent item) {
				return mapMainGroup(item);
			}
		});
	}

	public static KleMainGroup mapMainGroup(HovedgruppeKomponent input) {
		final String number = input.getHovedgruppeNr();
		final String title = input.getHovedgruppeTitel();
		final String description = buildDescription(input.getHovedgruppeVejledning());
		final Date dateCreated = dateFrom(input.getHovedgruppeAdministrativInfo().getOprettetDato());

		final List<KleGroup> groups = Lists.transform(input.getGruppe(), new Function<GruppeKomponent, KleGroup>() {
			public KleGroup apply(GruppeKomponent item) {
				return mapGroup(item);
			}
		});
		return new KleMainGroup(number, title, description, dateCreated, groups);
	}

	public static KleGroup mapGroup(GruppeKomponent input) {
		final String number = input.getGruppeNr();
		final String title = input.getGruppeTitel();
		final String description = buildDescription(input.getGruppeVejledning());
		final Date dateCreated = dateFrom(input.getGruppeAdministrativInfo().getOprettetDato());

		final List<KleTopic> topics = Lists.transform(input.getEmne(), new Function<EmneKomponent, KleTopic>() {
			public KleTopic apply(EmneKomponent item) {
				return mapTopic(item);
			}
		});
		return new KleGroup(number, title, description, dateCreated, topics);
	}

	public static KleTopic mapTopic(EmneKomponent input) {
		final String number = input.getEmneNr();
		final String title = input.getEmneTitel();
		final String description = buildDescription(input.getEmneVejledning());
		final Date dateCreated = dateFrom(input.getEmneAdministrativInfo().getOprettetDato());

		return new KleTopic(number, title, description, dateCreated);
	}

	public static String buildDescription(VejledningKomponent vejledning) {
		//TODO: implement!
		return "To be implemented";
	}

	public static Date dateFrom(XMLGregorianCalendar input) {
		return input.toGregorianCalendar().getTime();
	}
}
