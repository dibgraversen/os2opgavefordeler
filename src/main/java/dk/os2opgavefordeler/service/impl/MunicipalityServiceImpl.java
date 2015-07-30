package dk.os2opgavefordeler.service.impl;

import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.service.MunicipalityService;
import dk.os2opgavefordeler.service.PersistenceService;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * @author hlo@miracle.dk
 */
@Stateless
public class MunicipalityServiceImpl implements MunicipalityService {
	@Inject
	PersistenceService persistence;

	@Override
	public Municipality createMunicipality(Municipality municipality) {
		persistence.persist(municipality);
		return municipality;
	}

	@Override
	public Municipality getMunicipality(int id) {
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
		return (Municipality) query.getSingleResult();
	}

	private EntityManager getEm(){
		return persistence.getEm();
	}
}
