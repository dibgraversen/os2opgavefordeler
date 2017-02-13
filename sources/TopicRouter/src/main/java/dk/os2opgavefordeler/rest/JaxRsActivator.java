package dk.os2opgavefordeler.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import dk.os2opgavefordeler.distribution.DistributionRuleFilterEndpoint;
import dk.os2opgavefordeler.orgunit.ImportEndpoint;


/**
 * A class extending {@link Application} and annotated with @ApplicationPath is the Java EE 6
 * "no XML" approach to activating JAX-RS.
 * <p>
 * <p>
 * Resources are served relative to the servlet path specified in the {@link ApplicationPath}
 * annotation.
 * </p>
 */
@ApplicationPath("/rest")
public class JaxRsActivator extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> s = new HashSet<Class<?>>();
        s.add(DistributionRuleEndpoint.class);
        s.add(DistributionRuleFilterEndpoint.class);
        s.add(ApiEndpoint.class);
        s.add(EmploymentEndpoint.class);
        s.add(KleRestEndpoint.class);
        s.add(MunicipalityEndpoint.class);
        s.add(OrgUnitEndpoint.class);
        s.add(RoleEndpoint.class);
        s.add(SearchEndpoint.class);
        s.add(UserEndpoint.class);
        s.add(AuthEndpoint.class);
        s.add(ImportEndpoint.class);
        s.add(AuditLogEndpoint.class);
        s.add(OUEndpoint.class);
        return s;
    }

}
