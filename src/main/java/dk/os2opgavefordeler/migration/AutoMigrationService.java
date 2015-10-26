package dk.os2opgavefordeler.migration;

import org.flywaydb.core.Flyway;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class AutoMigrationService implements Integrator {

    @Override
    public void integrate(final Configuration configuration, final SessionFactoryImplementor sessionFactoryImplementor, final SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
        try {
            Context context = new InitialContext();
            final Flyway flyway = new Flyway();
            flyway.setDataSource((DataSource) context.lookup("java:/OS2TopicRouterDS"));
            flyway.migrate();
        } catch (NamingException ne) {

        }
    }

    @Override
    public void integrate(final MetadataImplementor metadataImplementor, final SessionFactoryImplementor sessionFactoryImplementor, final SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
        //no-op
    }

    @Override
    public void disintegrate(final SessionFactoryImplementor sessionFactoryImplementor, final SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
        //no-op
    }
}
