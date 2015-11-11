package dk.os2opgavefordeler;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

public class EntityManagerProducer {

   /* @PersistenceContext(unitName = "OS2TopicRouter")
    private EntityManager topicRouterEM;

    @Produces
    @Default
    public EntityManager create() {
        return topicRouterEM;
    }

    public void dispose(@Disposes @Default EntityManager entityManager) {

    }*/

    @PersistenceUnit(unitName = "OS2TopicRouter")
    private EntityManagerFactory emf;

    @Produces
    @Default
    @RequestScoped
    public EntityManager create() {
        return emf.createEntityManager();
    }

    public void close(@Disposes EntityManager em) {
        if (em.isOpen()) {
            em.close();
        }
    }
}

