package dk.os2opgavefordeler.service.impl;

import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.service.MunicipalityService;
import dk.os2opgavefordeler.service.PersistenceService;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import java.util.List;

/**
 * @author hlo@miracle.dk
 */
@Stateless
public class MunicipalityServiceImpl implements MunicipalityService {
	@Inject
	PersistenceService persistence;

	@Inject
	Logger logger;

	@Override
	public Municipality createMunicipality(Municipality municipality) {
		persistence.persist(municipality);
		return municipality;
	}

	@Override
	public Municipality createOrUpdateMunicipality(Municipality municipality) {
		if(municipality.getId() > 0l){
			persistence.merge(municipality);
		} else {
			persistence.persist(municipality);
		}
		return municipality;
	}

	@Override
	public Municipality getMunicipality(long id) {
		return getEm().find(Municipality.class, id);
	}

	@Override
	public List<Municipality> getMunicipalities() {
		return persistence.findAll(Municipality.class);
	}

	@Override
	public Municipality findByName(String name){
		Query query = getEm().createQuery("SELECT m FROM Municipality m WHERE m.name = :name");
		query.setParameter("name", name);
		Municipality result = new Municipality();
		try {
			result = (Municipality) query.getSingleResult();
		} catch	(NonUniqueResultException nure){
			logger.error("Duplicate name for municipality", nure);
		}
		return result;
	}

	private EntityManager getEm(){
		return persistence.getEm();
	}
}
