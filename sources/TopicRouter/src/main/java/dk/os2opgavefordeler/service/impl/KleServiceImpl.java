package dk.os2opgavefordeler.service.impl;

import dk.os2opgavefordeler.model.Kle;
import dk.os2opgavefordeler.model.Kle_;
import dk.os2opgavefordeler.service.KleService;
import dk.os2opgavefordeler.service.PersistenceService;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.Query;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class KleServiceImpl implements KleService {
	@Inject
	private Logger log;

	@Inject
	PersistenceService persistence;

	/**
	 * Fetches all 'main groups'.
	 * Municipality scoping not needed here since main groups cannot be municipality specific.
	 * @return List of all 'main groups'. Groups with no parent.
	 */
	@Override
	public List<Kle> fetchAllKleMainGroups() {
		List<Kle> result = persistence.criteriaFind(Kle.class,
			(cb, cq, ent) -> cq.where(cb.isNull(ent.get(Kle_.parent)))
		);
		return touchChildren(result);
	}

	@Override
	public Optional<Kle> fetchMainGroup(final String number, long municipalityId) {
		Query query = persistence.getEm().createQuery("SELECT k FROM Kle k WHERE k.number = :number AND " +
				"(k.municipality IS NULL OR k.municipality.id = :municipalityId)");
		query.setParameter("number", number);
		query.setParameter("municipalityId", municipalityId);
		try {
			Kle result = (Kle) query.getSingleResult();
			return Optional.of(result);
		} catch	(Exception e){
			return Optional.empty();
		}
	}

	@Override
	@Transactional
	public void storeAllKleMainGroups(List<Kle> groups) {
		//TODO: do we want to drop existing KLEs here? Probably not. For production, we need more complex logic than
		//a simple drop-and-create - this method, as it is now, is suitable only for testing.

		log.info("Persisting new KLE");
		groups.forEach(persistence::persist);
	}

	@Override
	public Kle getKle(Long id) {
		Query query = persistence.getEm().createQuery("SELECT k FROM Kle k WHERE k.id = :id");
		query.setParameter("id", id);
		return (Kle) query.getSingleResult();
	}
	
	@Override
	public Kle getKle(String kleNumber) {
		Query query = persistence.getEm().createQuery("SELECT k FROM Kle k WHERE k.number = :kleNumber");
		query.setParameter("kleNumber", kleNumber);
		return (Kle) query.getSingleResult();
	}

	private List<Kle> touchChildren(List<Kle> kle) {
		kle.forEach(k -> touchChildren(k.getChildren()));
		return kle;
	}
}
