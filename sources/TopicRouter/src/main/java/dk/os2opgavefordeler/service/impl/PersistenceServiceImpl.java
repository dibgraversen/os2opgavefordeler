package dk.os2opgavefordeler.service.impl;

import dk.os2opgavefordeler.service.PersistenceService;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@ApplicationScoped
@Transactional
public class PersistenceServiceImpl implements PersistenceService {
    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    @Override
    public <T> void persist(T entity) {
       // em.getTransaction().begin();
        em.persist(entity);
        em.flush();
       // em.getTransaction().commit();
    }

    @Override
    public <T> T merge(T entity) {
     //   em.getTransaction().begin();
        T merge = em.merge(entity);
     //   em.getTransaction().commit();
        return merge;
    }

    @Override
    public <T> List<T> criteriaFind(Class<T> clazz, CriteriaOp op) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<T> cq = cb.createQuery(clazz);
        final Root<T> ent = cq.from(clazz);

        op.apply(cb, cq, ent);

        final List<T> results = em.createQuery(cq).getResultList();
        return results;
    }

    @Override
    public <T> T find(Class<T> clazz, Object o) {
        return em.find(clazz, o);
    }

    @Override
    public <T> List<T> findAll(final Class<T> clazz) {
        return criteriaFind(clazz,
                (cb, cq, root) -> cq.select(root)
        );
    }

    @Override
    public void rollbackTransaction() {
        em.getTransaction().rollback();
    }

    public EntityManager getEm() {
        return em;
    }
}
