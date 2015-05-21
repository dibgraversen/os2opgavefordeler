package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Kle;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
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
	public <T> List<T> criteriaFind(Class clazz, CriteriaOp op) {
		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<T> cq = cb.createQuery(clazz);

		op.apply(cb, cq);

		return em.createQuery(cq).getResultList();
	}



	private void touchChildren(List<Kle> kle) {
		for (Kle k : kle) {
			touchChildren(k.getChildren());
		}
	}

	//TODO: move KLE specific stuff to a KLE service.
	@Override
	public List<Kle> fetchAllKleMainGroups() {
		final Query query = em.createQuery("SELECT e FROM Kle e");
		final List<Kle> result = query.getResultList();
		//FIXME: this is a workaround for EntityManager/session lifetime and lazyload...
		touchChildren(result);

		return result;
	}

	@Override
	public Kle fetchMainGroup(String number) {
		final Query query = em.createQuery("SELECT e FROM Kle e where e.number = :number");
		query.setParameter("number", number);
		try {
			return (Kle) query.getSingleResult();
		}
		catch(NoResultException ex) {
			log.warn("fetchMainGroup: no results for [{}]", number, ex);
			//TODO: Java8 Optional?
			return null;
		}
	}

	@Override
	public void storeAllKleMainGroups(List<Kle> groups) {
		log.info("Deleting existing KLE");
		em.createQuery(String.format("DELETE FROM %s", Kle.TABLE_NAME)).executeUpdate();
		log.info("Persisting new KLE");
		for (Kle group : groups) {
			em.persist(group);
		}
	}
}
