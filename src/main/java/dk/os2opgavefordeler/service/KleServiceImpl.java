package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Kle;
import dk.os2opgavefordeler.model.Kle_;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

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
	public Optional<Kle> fetchMainGroup(final String number) {
		List<Kle> result = persistence.criteriaFind(Kle.class,
			(cb, cq, kle) -> cq.where( cb.equal(kle.get(Kle_.number), number))
		);
		return result.isEmpty() ?
			Optional.empty() :
			Optional.of(result.get(0));
	}

	@Override
	public void storeAllKleMainGroups(List<Kle> groups) {
		//TODO: do we want to drop existing KLEs here? Probably not. For production, we need more complex logic than
		//a simple drop-and-create - this method, as it is now, is suitable only for testing.

		log.info("Persisting new KLE");
		groups.forEach(persistence::persist);
	}


	private List<Kle> touchChildren(List<Kle> kle) {
		kle.forEach(k -> touchChildren(k.getChildren()));
		return kle;
	}
}
