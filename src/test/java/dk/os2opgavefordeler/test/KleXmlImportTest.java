package dk.os2opgavefordeler.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import dk.os2opgavefordeler.model.kle.KleGroup;
import dk.os2opgavefordeler.model.kle.KleMainGroup;
import dk.os2opgavefordeler.model.kle.KleTopic;
import dk.os2opgavefordeler.service.KleImportService;
import dk.os2opgavefordeler.service.KleImportServiceImpl;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class KleXmlImportTest {
	private static List<KleMainGroup> groups;

	@BeforeClass
	public static void importValidXml()
	throws Exception
	{
		final File folder = new File("src/test/resources");
		try(
				FileInputStream kleXsd = new FileInputStream(new File(folder, "KLE-Emneplan-version-2-0.xsd"));
				FileInputStream kleXml = new FileInputStream(new File(folder, "KLE-valid-data.xml"))
		) {
			final KleImportService is = new KleImportServiceImpl();
			groups = is.importFromXml(kleXml, kleXsd);
		}
	}

	@Test
	public void testKleMainGroup() {
		assertEquals("Incorrect number of main groups", 3, groups.size());
		final KleMainGroup mgroup = groups.get(1);
		assertEquals("Incorrect KLE-number", "13", mgroup.getNumber());
		assertEquals("Incorrect title", "Forsyning", mgroup.getTitle());
	}

	@Test
	public void testKleGroup() {
		final KleMainGroup mgroup = groups.get(1);

		assertEquals("Incorrect number of groups", 5, mgroup.getGroups().size(), 5);
		final KleGroup group = mgroup.getGroups().get(3);
		assertEquals("Incorrect KLE-number", "13.03", group.getNumber());
		assertEquals("Incorrect title", "Varmeforsyning", group.getTitle());
	}

	@Test
	public void testKleTopic() {
		final KleMainGroup mgroup = groups.get(1);
		final KleGroup group = mgroup.getGroups().get(3);

		assertEquals("Incorrect number of topics", 13, group.getTopics().size());
		final KleTopic topic = group.getTopics().get(11);
		assertEquals("Incorrect KLE-number", "13.03.24", topic.getNumber());
		assertEquals("Incorrect title", "Ekspropriation til varmeforsyning", topic.getTitle());
	}

	@Test(expected = Exception.class)
	public void testImportInvalidXml()
	throws Exception
	{
		final File folder = new File("src/test/resources");
		try(
				FileInputStream kleXsd = new FileInputStream(new File(folder, "KLE-Emneplan-version-2-0.xsd"));
				FileInputStream kleXml = new FileInputStream(new File(folder, "KLE-error-data.xml"))
		) {
			final KleImportService is = new KleImportServiceImpl();
			is.importFromXml(kleXml, kleXsd);
		}
	}

	@Test
	public void testKleDescription() {
		final String description =
		"<p>Her journaliseres sager vedrørende:</p>" +
		"<p>" +
			"<ul>" +
				"<li>internationale organisationer som fx: FN, UNESCO og Røde Kors, som ikke kan knyttes nærmere til et konkret emne</li>" +
				"<li>organisering og udvikling af hjælpeorganisationer</li>" +
			"</ul>" +
		"</p>";

		final KleTopic topic = findTopic("00.03.02");
		assertEquals("Wrong description", description, topic.getDescription());
	}

	public static KleTopic findTopic(String topicNum) {
		final String[] parts = topicNum.split("\\.");

		return FluentIterable.from(groups)
		.firstMatch(new Predicate<KleMainGroup>() {
			@Override
			public boolean apply(KleMainGroup kleMainGroup) {
				return kleMainGroup.getNumber().equals(parts[0]);
			}
		})
		.transform(new Function<KleMainGroup, FluentIterable<KleGroup>>() {
			@Override
			public FluentIterable<KleGroup> apply(KleMainGroup mainGroup) {
				return FluentIterable.from(mainGroup.getGroups());
			}
		}).get()
		.firstMatch(new Predicate<KleGroup>() {
			@Override
			public boolean apply(KleGroup group) {
				return group.getNumber().substring(3).equals(parts[1]);
			}
		})
		.transform(new Function<KleGroup, FluentIterable<KleTopic>>() {
			@Override
			public FluentIterable<KleTopic> apply(KleGroup kleGroup) {
				return FluentIterable.from(kleGroup.getTopics());
			}
		}).get()
		.firstMatch(new Predicate<KleTopic>() {
			@Override
			public boolean apply(KleTopic topic) {
				return topic.getNumber().substring(6).equals(parts[2]);
			}
		})
		.orNull();
	}
}
