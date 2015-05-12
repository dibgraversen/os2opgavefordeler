package dk.os2opgavefordeler.service;

import com.google.common.collect.ImmutableList;
import dk.os2opgavefordeler.model.kle.KleGroup;
import dk.os2opgavefordeler.model.kle.KleMainGroup;
import dk.os2opgavefordeler.model.kle.KleTopic;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Stateless
@TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
public class PersistenceServiceImpl implements PersistenceService {
	@Inject
	private Logger log;

	@PersistenceContext(unitName = "OS2TopicRouter")
	private EntityManager em;

	@Override
	public List<KleMainGroup> fetchAllKleMainGroups() {
		final Query query = em.createQuery("SELECT e FROM KleMainGroup e");
		log.info("Executing query");
		final List<KleMainGroup> result = query.getResultList();
		//FIXME: this is a workaround for EntityManager/session lifetime and lazyload...
		for (KleMainGroup group : result) {
			group.getGroups().size();
		}
		log.info("Returning result");

		return result;
	}

	@Override
	public KleMainGroup fetchMainGroup(String number) {
		final Query query = em.createQuery("SELECT e FROM KleMainGroup e where e.number = :number");
		query.setParameter("number", number);
		try {
			return (KleMainGroup) query.getSingleResult();
		}
		catch(NoResultException ex) {
			log.warn("fetchMainGroup: no results for [{}]", number, ex);
			//TODO: Java8 Optional?
			return null;
		}
	}

	@Override
	public void storeAllKleMainGroups(List<KleMainGroup> groups) {
		log.info("Deleting existing KLE");
		final ImmutableList<String> tables = ImmutableList.of(
			KleTopic.TABLE_NAME, KleGroup.TABLE_NAME, KleMainGroup.TABLE_NAME
		);
		for (String table : tables) {
			//Named parameters don't seem to be supported for table named - this feels slightly dirty, but we'll live
			// since there's no user-controlled data involved.
			em.createQuery(String.format("DELETE FROM %s", table)).executeUpdate();
		}

		log.info("Persisting new KLE");
		for (KleMainGroup group : groups) {
			em.persist(group);
		}
	}
}
