package dk.os2opgavefordeler.service;

import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Stateless
@TransactionAttribute(value = TransactionAttributeType.REQUIRED)
public class PersistenceServiceImpl implements PersistenceService {
	@Inject
	private Logger log;

	@PersistenceContext(unitName = "OS2TopicRouter")
	private EntityManager em;

	@Override
	public <T> void persist(T entity) {
		em.persist(entity);
	}

	@Override
	public <T> List<T> criteriaFind(Class<T> clazz, CriteriaOp op) {
		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<T> cq = cb.createQuery(clazz);
		final Root<T> ent = cq.from(clazz);

		op.apply(cb, cq, ent);

		return em.createQuery(cq).getResultList();
	}

	@Override
	public <T> List<T> findAll(final Class<T> clazz) {
		return criteriaFind(clazz,
			(cb, cq, ent) -> cq.select(cq.from(clazz))
		);
	}
}
