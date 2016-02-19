package dk.os2opgavefordeler.test;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.persistence.*;

@Default
@Alternative
public class TestEntityManagerProducer {

    private EntityManager entityManager;

    @Produces
    @Alternative
    @RequestScoped
    public EntityManager create() {
        return Persistence.createEntityManagerFactory("test-db").createEntityManager();
    }

    protected void closeEntityManager(@Disposes EntityManager entityManager) {
        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }
}
