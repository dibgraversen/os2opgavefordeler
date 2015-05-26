package dk.os2opgavefordeler.rest;

import com.google.common.collect.Sets;
import dk.os2opgavefordeler.service.DistributionService;
import dk.os2opgavefordeler.service.KleService;
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
	@Inject
	Logger log;

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
			with("Has direct and unowned", this::dist_hasDirectAndUnowned),
			with("Has only unowned", this::dist_hasOnlyUnowned)
		);

		return result.toString();
	}

	@Test
	private void dist_hasNone(StringBuilder result) {
		assertTrue(result,
			!getKleIds(42, false).findAny().isPresent());
	}

	@Test
	private void dist_hasDirectlyOwnedOne(StringBuilder result) {
		assertMembership(result,
			Stream.of("13"),
			getKleIds(1, false)
		);
		result.append("\n");
	}

	@Test
	private void dist_hasDirectlyOwnedTwo(StringBuilder result) {
		assertMembership(result,
			Stream.of("14", "14.00"),
			getKleIds(2, false)
		);
		result.append("\n");
	}

	@Test
	private void dist_hasDirectAndUnowned(StringBuilder result) {
		assertMembership(result,
			Stream.of("13", "00", "00.01", "00.01.00"),
			getKleIds(1, true)
		);
	}

	@Test
	private void dist_hasOnlyUnowned(StringBuilder result) {
		assertMembership(result,
			Stream.of("00", "00.01", "00.01.00"),
			getKleIds(42, true)
		);
	}



	// -------------------------------------------------------------------------
	// Helper methods
	// -------------------------------------------------------------------------
	@interface Test {}	// Temporary annotation until we're running under Arquillian.

	class Zest {
		public final String name;
		public final Consumer<StringBuilder> method;
		Zest(String name, Consumer<StringBuilder> method) {
			this.name = name;
			this.method = method;
		}
	}
	public Zest with(String name, Consumer<StringBuilder> method) {
		return new Zest(name, method);
	}

	private void runSuite(StringBuilder output, Zest... testsToRun) {
		for (Zest test : testsToRun) {
			output.append("Test of '").append(test.name).append("': [\n");
			test.method.accept(output);
			output.append("]\n");
		}
	}

	private void assertTrue(StringBuilder out, boolean value) {
		out.append("\t[").append(value ? "PASS" : "FAIL").append("]\n");
	}

	private void assertMembership(StringBuilder out, Stream<String> expectedIn, Stream<String> valuesIn) {
		Set<String> expected = expectedIn.collect(Collectors.toSet());
		Set<String> values = valuesIn.collect(Collectors.toSet());

		if(values.equals(expected)) {
			out.append("\t[PASS]");
		} else {
			log.info("EXPECTED: {}", expected);
			log.info("ACTUALLY: {}", values);

			out.append("\t[FAIL] {\n");
			out.append("\t\t=").append(Sets.intersection(values, expected)).append("\n");
			out.append("\t\t+").append(Sets.difference(values, expected)).append("\n");
			out.append("\t\t-").append(Sets.difference(expected, values)).append("\n\t}\n");
		}
	}

	private Stream<String> getKleIds(int orgId, boolean include) {
		return distributionService.getDistributionsForOrg(orgId, include)
			.stream()
			.map(d -> d.getKle().getNumber());
	}
}
