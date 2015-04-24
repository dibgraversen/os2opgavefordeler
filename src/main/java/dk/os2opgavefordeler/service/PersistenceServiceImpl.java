package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.kle.KleMainGroup;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Stateless
public class PersistenceServiceImpl implements PersistenceService {
	@PersistenceContext(unitName = "OS2TopicRouter")
	private EntityManager em;

	@Override
	public List<KleMainGroup> fetchAllKleMainGroups() {
		final Query query = em.createQuery("SELECT e FROM KleMainGroup e");
		final List<KleMainGroup> result = query.getResultList();

		return result;
	}

	@Override
	public void storeAllKleMainGroups(List<KleMainGroup> groups) {
		em.persist(groups);
	}
}
