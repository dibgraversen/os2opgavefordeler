package dk.os2opgavefordeler.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import dk.os2opgavefordeler.model.Kle;
import dk.os2opgavefordeler.model.kle_import.*;
import dk.os2opgavefordeler.util.FilteringXMLStreamWriter;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
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

/**
 * Maps the unmarshalled object model (jxc compiled from .xsd) to our own, simpler, domain model.
 *
 * A couple of *important* notes with regards to performance:
 * 1) Guava Lists.transform is a *computed view* - it works by applying the transform on every access.
 *    To avoid the constant computations, we instantiate the transforms to a concrete list in mapMainGroupList().
 *
 * 2) JAXB Context creation is very expensive. Moving context creation from buildDescription() to mapMainGroupList()
 *    reduced mapping time from ~25s down to ~1s.
 *
 * TODO: should we drop entries with a non-empty 'Udgaaet' date, or should we add dateExpired to the model?
 */
@Stateless
@Dependent
public class KleImportMapperImpl implements KleImportMapper {
	private static final String FAKE_ROOT = "FakeRoot";
	private static final String REAL_ROOT = "VejledningTekst";
	private static final int DESC_LEN = 6000; 	// Description stringwriter buffer size, a bit larger than max in current dataset.

	private JAXBContext jContext;

	@PostConstruct
	void init() throws JAXBException {
		this.jContext = JAXBContext.newInstance(VejledningKomponent.class);
	}

	@Override
	public List<Kle> mapMainGroupList(KLEEmneplanKomponent input) {
		List<Kle> mainGroups = Lists.transform(input.getHovedgruppe(), this::mapMainGroup);

		return ImmutableList.copyOf(mainGroups);
	}

	private Kle mapMainGroup(HovedgruppeKomponent input) {
		final String number = input.getHovedgruppeNr();
		final String title = input.getHovedgruppeTitel();
		final String description = buildDescription(input.getHovedgruppeVejledning());
		final Date dateCreated = dateFrom(input.getHovedgruppeAdministrativInfo().getOprettetDato());

		final List<Kle> groups = Lists.transform(input.getGruppe(), this::mapGroup);
		return new Kle(number, title, description, dateCreated, groups);
	}

	private Kle mapGroup(GruppeKomponent input) {
		final String number = input.getGruppeNr();
		final String title = input.getGruppeTitel();
		final String description = buildDescription(input.getGruppeVejledning());
		final Date dateCreated = dateFrom(input.getGruppeAdministrativInfo().getOprettetDato());

		final List<Kle> topics = Lists.transform(input.getEmne(), this::mapTopic);
		return new Kle(number, title, description, dateCreated, topics);
	}

	private Kle mapTopic(EmneKomponent input) {
		final String number = input.getEmneNr();
		final String title = input.getEmneTitel();
		final String description = buildDescription(input.getEmneVejledning());
		final Date dateCreated = dateFrom(input.getEmneAdministrativInfo().getOprettetDato());

		return new Kle(number, title, description, dateCreated);
	}

	private String buildDescription(VejledningKomponent vejledning)
	{
		if(vejledning == null) {
			//TODO: should we allow description to be nullable in the db instead of this?
			return "";
		}
		try {
			// Instead of walking the somewhat ugly object graph, we cheat and marshal it with JAXB and gentle massage.
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
			final StringWriter result = new StringWriter(DESC_LEN);
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
