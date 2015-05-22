package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Kle;
import dk.os2opgavefordeler.model.Kle_;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class KleServiceImpl implements KleService {
	@Inject
	private Logger log;

	@Inject
	PersistenceService persistence;

	@Override
	public List<Kle> fetchAllKleMainGroups() {
		List<Kle> result = persistence.criteriaFind(Kle.class,
			(cb, cq, ent) -> cq.where(cb.isNull(ent.get(Kle_.parent)))
		);
		return touchChildren(result);
	}

	@Override
	public Kle fetchMainGroup(final String number) {
		List<Kle> result = persistence.criteriaFind(Kle.class,
			(cb, cq, kle) -> cq.where( cb.equal(kle.get(Kle_.number), number))
		);
		return result.get(0);
	}

	@Override
	public void storeAllKleMainGroups(List<Kle> groups) {
		log.info("Deleting existing KLE");
		//TODO: add necessary method to PersistenceService.
//		em.createQuery(String.format("DELETE FROM %s", Kle.TABLE_NAME)).executeUpdate();
		log.info("Persisting new KLE");

		groups.forEach(persistence::persist);
	}


	private List<Kle> touchChildren(List<Kle> kle) {
		kle.forEach(k -> touchChildren(k.getChildren()));
		return kle;
	}
}
