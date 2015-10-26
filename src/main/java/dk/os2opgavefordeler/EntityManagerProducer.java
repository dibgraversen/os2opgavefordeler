package dk.os2opgavefordeler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@ApplicationScoped
public class EntityManagerProducer {

    @PersistenceContext(unitName = "OS2TopicRouter")
    private EntityManager topicRouterEM;

    @Produces
    @Default
    public EntityManager create() {
        return topicRouterEM;
    }
}