package dk.os2opgavefordeler.service;

import com.google.common.collect.ImmutableList;
import dk.os2opgavefordeler.model.kle.KleGroup;
import dk.os2opgavefordeler.model.kle.KleMainGroup;
import dk.os2opgavefordeler.model.kle.KleTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Stateless
public class PersistenceServiceImpl implements PersistenceService {
	private final Logger log = LoggerFactory.getLogger(getClass());

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
