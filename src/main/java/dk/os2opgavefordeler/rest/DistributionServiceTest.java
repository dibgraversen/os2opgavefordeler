package dk.os2opgavefordeler.rest;

import com.google.common.collect.Sets;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.service.DistributionService;
import dk.os2opgavefordeler.service.KleService;
import dk.os2opgavefordeler.service.OrgUnitService;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * "Poor man's integration test".
 *
 * Getting EntityManager working with CDIUnit proved troublesome (and we don't want to mock it, since what we want to
 * test is largely the validity of our database queries). Arquillian seems like the proper route to go, but requires
 * some research + setup (especially on the Jenkins server). So for now, this is implemented as a REST service and a
 * homebrew mini testing framework.
 */
@Path("/test")
@RequestScoped
public class DistributionServiceTest {
	public static final int ORG_NONEXISTING = 42;
	public static final int ORG_1 = 1;
	public static final int ORG_2 = 2;

	@Inject
	Logger log;

	@Inject
	OrgUnitService orgUnitService;

	@Inject
	KleService kleService;

	@Inject
	DistributionService distributionService;

	@PostConstruct
	public void setup() {
		// log.info("DistributionServiceTest::setup");
		// This test code requires data from BootstrappingDataProviderSingleton, so there's no setup here.
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/suite")
	public String suite() {
		final StringBuilder result = new StringBuilder();

		runSuite(result,
			with("Has no assignments", this::dist_hasNone),
			with("Has directly owned, 1 assignment", this::dist_hasDirectlyOwnedOne),
			with("Has directly owned, 2 assignments", this::dist_hasDirectlyOwnedTwo),
			with("Has implicitly owned, 1 assignment", this::dist_hasImplicitlyOwnedOne),
			with("Has implicitly owned, 2 assignments", this::dist_hasImplicitlyOwnedTwo),
			with("Has direct and unowned", this::dist_hasDirectAndUnowned),
			with("Has only unowned", this::dist_hasOnlyUnowned)
		);

		return result.toString();
	}

	@Test
	private void dist_hasNone(StringBuilder result) {
		assertTrue(result,
			!getKleIds(ORG_NONEXISTING, false, false).findAny().isPresent());
	}

	@Test
	private void dist_hasDirectlyOwnedOne(StringBuilder result) {
		assertMembership(result,
			Stream.of("13"),
			getKleIds(ORG_1, false, false)
		);
	}

	@Test
	private void dist_hasDirectlyOwnedTwo(StringBuilder result) {
		assertMembership(result,
			Stream.of("14", "14.00"),
			getKleIds(ORG_2, false, false)
		);
	}

	@Test
	private void dist_hasImplicitlyOwnedOne(StringBuilder result) {
		assertMembership(result,
			Stream.of(
				"13",							// directly owned
				"13.00", "13.00.00"				// implicitly owned
			),
			getKleIds(ORG_1, false, true)
		);
	}

	@Test
	private void dist_hasImplicitlyOwnedTwo(StringBuilder result) {
		assertMembership(result,
			Stream.of(
				"14", "14.00",					// directly owned
				"14.00.01"),					// implicitly owned
			getKleIds(ORG_2, false, true)
		);
	}

	//TODO: has implicitly and unowned

	@Test
	private void dist_hasDirectAndUnowned(StringBuilder result) {
		assertMembership(result,
			Stream.of(
				"13",							// directly owned
				"00", "00.01", "00.01.00"),		// unowned
			getKleIds(ORG_1, true, false)
		);
	}

	@Test
	private void dist_hasOnlyUnowned(StringBuilder result) {
		assertMembership(result,
			Stream.of("00", "00.01", "00.01.00"),
			getKleIds(ORG_NONEXISTING, true, false)
		);
	}



	// -------------------------------------------------------------------------
	// Helper methods
	// -------------------------------------------------------------------------
	@interface Test {}	// Temporary annotation until we're running under Arquillian.

	class TestCase {
		public final String name;
		public final Consumer<StringBuilder> method;
		TestCase(String name, Consumer<StringBuilder> method) {
			this.name = name;
			this.method = method;
		}
	}
	public TestCase with(String name, Consumer<StringBuilder> method) {
		return new TestCase(name, method);
	}

	private void runSuite(StringBuilder output, TestCase... testsToRun) {
		for (TestCase test : testsToRun) {
			output.append("Test of '").append(test.name).append("': ");
			test.method.accept(output);
			output.append("\n\n");
		}
	}

	private void assertTrue(StringBuilder out, boolean value) {
		out.append("[").append(value ? "PASS" : "FAIL").append(']');
	}

	private void assertMembership(StringBuilder out, Stream<String> expectedIn, Stream<String> valuesIn) {
		Set<String> expected = expectedIn.collect(Collectors.toSet());
		Set<String> values = valuesIn.collect(Collectors.toSet());

		if(values.equals(expected)) {
			out.append("[PASS]");
		} else {
			out.append("[FAIL] {\n");
			out.append("\t=").append(Sets.intersection(values, expected)).append("\n");
			out.append("\t+").append(Sets.difference(values, expected)).append("\n");
			out.append("\t-").append(Sets.difference(expected, values)).append("\n}");
		}
	}

	private Stream<String> getKleIds(long orgId, boolean includeUnowned, boolean includeImplicit) {
		OrgUnit org;
		if(orgId == ORG_1) {
			org = orgUnitService.findByName("Digitalisering").get(0);
		} else if(orgId == ORG_2) {
			org = orgUnitService.findByName("Moderne kunst").get(0);
		} else {
			org = null;
		}
		if(org != null) {
			orgId = org.getId();
		}

		return distributionService.getDistributionsForOrg(orgId, includeUnowned, includeImplicit)
			.stream()
			.map(d -> d.getKle().getNumber());
	}
}
