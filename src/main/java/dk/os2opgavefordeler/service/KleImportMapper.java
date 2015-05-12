package dk.os2opgavefordeler.service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import dk.os2opgavefordeler.model.kle.KleGroup;
import dk.os2opgavefordeler.model.kle.KleMainGroup;
import dk.os2opgavefordeler.model.kle.KleTopic;
import dk.os2opgavefordeler.model.kle_import.*;
import dk.os2opgavefordeler.util.FilteringXMLStreamWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;

public class KleImportMapper {
	public static final String FAKE_ROOT = "FakeRoot";
	public static final String REAL_ROOT = "VejledningTekst";
	//TODO: should we drop entries with a non-empty 'Udgaaet' date, or should we add dateExpired to the model?

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

	public static String buildDescription(VejledningKomponent vejledning)
	{
		if(vejledning == null) {
			//TODO: should we allow description to be nullable in the db instead of this?
			return "";
		}
		try {
			// Instead of walking the somewhat ugly object graph, we cheat and marshal it with JAXB and gentle massage.
			final JAXBContext jContext = JAXBContext.newInstance(VejledningKomponent.class);
			final Marshaller marshaller = jContext.createMarshaller();

			// Avoid XML header in output.
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

			// We can't directly marshal VejledningKomponent since it's not annotated with @XmlRootElement, so we wrap
			// it with the "FakeRoot" element, which we *unfortunately can't* remove with the FilteringXMLStreamWriter.
			// Why? Because the underlying XMLOutputStream, not the JAXB marshaller, verifies that there's only one
			// root element... and we can have multiple <p> elements in our VejledningKomponent.
			// So we have to keep either the FakeRoot or the VejledningTekst element. We could use an innocuous <div>
			// or <p> instead of <FakeRoot>, but opted for <FakeRoot> + removal.
			JAXBElement<VejledningKomponent> root = new JAXBElement<>(
				new QName(FAKE_ROOT), VejledningKomponent.class, vejledning
			);

			// Marshal to string through our filter - strip namespaces and whitespace.
			final StringWriter result = new StringWriter();
			try(FilteringXMLStreamWriter fxsw = FilteringXMLStreamWriter.wrap(result, true, true, REAL_ROOT)) {
				marshaller.marshal(root, fxsw);
			}

			return removeTag(result.toString(), FAKE_ROOT);
		}
		catch(JAXBException|XMLStreamException|StringIndexOutOfBoundsException ex) {
			throw new RuntimeException("Error building KLE description", ex);
		}
	}

	private static String removeTag(String text, String tag) {
		// +2 for open+close brackets, +3 for brackets and tag-termination-slash.
		return text.substring(tag.length() + 2, text.length() - tag.length() - 3);
	}

	public static Date dateFrom(XMLGregorianCalendar input) {
		return input.toGregorianCalendar().getTime();
	}
}
