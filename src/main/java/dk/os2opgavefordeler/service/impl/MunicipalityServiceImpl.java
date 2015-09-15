package dk.os2opgavefordeler.service.impl;

import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.service.MunicipalityService;
import dk.os2opgavefordeler.service.PersistenceService;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import java.util.List;
import java.util.Optional;

/**
 * @author hlo@miracle.dk
 */
@Stateless
public class MunicipalityServiceImpl implements MunicipalityService {
	@Inject
	PersistenceService persistence;

	@Inject
	Logger log;

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
	@SuppressWarnings("unchecked")
	public Optional<Municipality> getMunicipalityFromToken(String token){
		Query query = persistence.getEm().createQuery("SELECT m FROM Municipality m WHERE m.token = :token");
		query.setParameter("token", token);
		try{
			return Optional.of((Municipality)query.getSingleResult());
		} catch(NoResultException nre){
			log.info("trying to find by token: {}, without luck", token);
		} catch(NonUniqueResultException nure){
			log.error("Multiple municipalities with same token: {}", token);
		}
		return Optional.empty();
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
			log.error("Duplicate name for municipality", nure);
		}
		return result;
	}

	private EntityManager getEm(){
		return persistence.getEm();
	}
}
