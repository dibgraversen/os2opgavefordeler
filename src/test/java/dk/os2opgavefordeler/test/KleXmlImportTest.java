package dk.os2opgavefordeler.test;

import dk.os2opgavefordeler.model.Kle;
import dk.os2opgavefordeler.service.KleImportService;
import dk.os2opgavefordeler.service.impl.KleImportServiceImpl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

@Category(UnitTest.class)
@RunWith(CdiTestRunner.class)
public class KleXmlImportTest {
    private static List<Kle> groups;

    @Inject
    KleImportService kleImport;

    @Before
    public void importValidXml()
            throws Exception {
        if (groups != null) {
            // only load the test KLE once.
            return;
        }

        final File folder = new File("src/test/resources");
        try (
                FileInputStream kleXsd = new FileInputStream(new File(folder, "KLE-Emneplan-version-2-0.xsd"));
                FileInputStream kleXml = new FileInputStream(new File(folder, "KLE-valid-data.xml"))
        ) {
            groups = kleImport.importFromXml(kleXml, kleXsd);
        }
    }

    @Test
    public void testKleMainGroup() {
        assertEquals("Incorrect number of main groups", 3, groups.size());
        final Kle mgroup = groups.get(1);
        assertEquals("Incorrect KLE-number", "13", mgroup.getNumber());
        assertEquals("Incorrect title", "Forsyning", mgroup.getTitle());
    }

    @Test
    public void testKleGroup() {
        final Kle mgroup = groups.get(1);

        assertEquals("Incorrect number of groups", 5, mgroup.getChildren().size(), 5);
        final Kle group = mgroup.getChildren().get(3);
        assertEquals("Incorrect KLE-number", "13.03", group.getNumber());
        assertEquals("Incorrect title", "Varmeforsyning", group.getTitle());
    }

    @Test
    public void testKleTopic() {
        final Kle mgroup = groups.get(1);
        final Kle group = mgroup.getChildren().get(3);

        assertEquals("Incorrect number of topics", 13, group.getChildren().size());
        final Kle topic = group.getChildren().get(11);
        assertEquals("Incorrect KLE-number", "13.03.24", topic.getNumber());
        assertEquals("Incorrect title", "Ekspropriation til varmeforsyning", topic.getTitle());
    }

    @Test(expected = Exception.class)
    public void testImportInvalidXml()
            throws Exception {
        final File folder = new File("src/test/resources");
        try (
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

        final Optional<Kle> topic = findTopic("00.03.02");
        assertEquals("Wrong description", description, topic.map(Kle::getDescription).orElse("<KLE not found>"));
    }

    public Optional<Kle> findTopic(final String topicNum) {
        final String mainNum = topicNum.substring(0, 2);
        final String subNum = topicNum.substring(0, 5);

        return groups.stream()
                .filter(main -> mainNum.equals(main.getNumber()))
                .flatMap(main -> main.getChildren().stream())
                .filter(sub -> subNum.equals(sub.getNumber()))
                .flatMap(sub -> sub.getChildren().stream())
                .filter(topic -> topicNum.equals(topic.getNumber()))
                .findFirst();
    }
}
