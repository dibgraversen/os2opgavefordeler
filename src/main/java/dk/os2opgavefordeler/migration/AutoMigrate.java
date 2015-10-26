package dk.os2opgavefordeler.migration;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@ApplicationScoped
@Eager
@Named
public class AutoMigrate {

    @Inject
    private Logger logger;

    @PostConstruct
    private void init() {
        try {
            InitialContext context = new InitialContext();
            final Flyway flyway = new Flyway();
            flyway.setDataSource((DataSource) context.lookup("java:/OS2TopicRouterDS"));
            flyway.migrate();
        } catch (NamingException ne) {
            logger.error(ne.getMessage(), ne);
        }
    }
}