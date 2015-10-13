package dk.os2opgavefordeler.service.impl;

import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.Kle;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.ValidationException;
import dk.os2opgavefordeler.model.presentation.KlePO;
import dk.os2opgavefordeler.service.DistributionService;
import dk.os2opgavefordeler.service.KleService;
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
import java.util.stream.Collectors;

/**
 * @author hlo@miracle.dk
 */
@Stateless
public class MunicipalityServiceImpl implements MunicipalityService {
	@Inject
	PersistenceService persistence;

	@Inject
	Logger log;

	@Inject
	KleService kleService;

	@Inject
	DistributionService distributionService;

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

	/**
	 * Finds all municipality-specific kle.
	 * @param municipalityId id of the municipality.
	 * @return list of kle for municipality.
	 */
	@SuppressWarnings("unchecked")
	public List<KlePO> getMunicipalityKle(long municipalityId){
		Query query = getEm().createQuery("SELECT k FROM Kle k WHERE k.municipality.id = :municipalityId ORDER BY k.number ASC");
		query.setParameter("municipalityId", municipalityId);
		List<Kle> kles = query.getResultList();
		return kles.stream().map(KlePO::new).collect(Collectors.toList());
	}

	@Override
	public KlePO saveMunicipalityKle(KlePO kle) throws ValidationException {
		// validations.
		KlePO result = null;
		Kle parent = validateAndReturnParent(kle);
		Optional<Kle> targetMaybe = kleService.fetchMainGroup(kle.getNumber(), kle.getMunicipalityId());
		if(targetMaybe.isPresent()){
			if(targetMaybe.get().getId() != kle.getId()){
				throw new ValidationException("Angivne nummer anvendes allerede af anden kle.");
			}
		}
		Municipality municipality = getMunicipality(kle.getMunicipalityId());
		if(kle.getId() > 0l){ // update
			Kle existing = kleService.getKle(kle.getId());
			existing.setNumber(kle.getNumber());
			existing.setTitle(kle.getName());
			existing.setDescription(kle.getServiceText());
			existing.setParent(parent);
			getEm().merge(existing);
			if(existing.getChildren() != null && existing.getChildren().size() > 0){
				String main = existing.getNumber().split("\\.")[0];
				String group = existing.getNumber().split("\\.")[1];
				for (Kle child : existing.getChildren()) {
					String topic = child.getNumber().split("\\.")[2];
					String newNumber = main + "." + group + "." + topic;
					child.setNumber(newNumber);
					getEm().merge(child);
				}
			}
			result = new KlePO(existing);
		} else {  // new object
			Kle newKle = (kle.asKle());
			newKle.setMunicipality(municipality);
			newKle.setParent(parent);
			getEm().persist(newKle);
			result = new KlePO(newKle);
			distributionService.createDistributionRule(newKle);
		}
		return result;
	}

	@Override
	public void deleteMunicipalityKle(long municipalityId, long kleId) throws ValidationException {
		Kle kle = null;
		try	{
			Query query = persistence.getEm().createQuery("SELECT k from Kle k WHERE municipality.id = :municipalityId AND id = :kleId");
			query.setParameter("municipalityId", municipalityId);
			query.setParameter("kleId", kleId);
			kle = (Kle) query.getSingleResult();
		} catch (Exception e){
			throw new ValidationException("Did not find single result for Kle lookup");
		}
		if(kle.getChildren() != null && kle.getChildren().size() > 0){
			throw new ValidationException("Kan ikke slette grupper med tilknyttede emner");
		} else {
			Optional<DistributionRule> ruleMaybe = getRule(kle);
			if(ruleMaybe.isPresent()){
				DistributionRule rule = ruleMaybe.get();
				persistence.getEm().remove(rule);
			}
			persistence.getEm().remove(kle);
		}
	}

	private Optional<DistributionRule> getRule(Kle kle){
		Query query = persistence.getEm().createQuery("SELECT r FROM DistributionRule r WHERE r.kle = :kle");
		query.setParameter("kle", kle);
		try {
			return Optional.of((DistributionRule) query.getSingleResult());
		} catch (Exception e){
			return Optional.empty();
		}
	}

	private Kle validateAndReturnParent(KlePO kle) throws ValidationException {
		String number = kle.getNumber();
		String[] parts = number.split("\\.");
		String main = parts[0];
		if(kle.getName() == null || kle.getName().length() < 1){
			throw new ValidationException("Der skal angives titel.");
		}
		Optional<Kle> mainMaybe = kleService.fetchMainGroup(main, kle.getMunicipalityId());
		if(!mainMaybe.isPresent()){
			throw new ValidationException("Kunne ikke finde hovedgruppe ud fra nummer. Der kan kun oprettes kle for eksisterende hovedgrupper");
		}
		if(parts.length < 2 || parts.length > 3){
			throw new ValidationException("Nummer skal være 2-3 grupper af tal, adskilt af '.'");
		}
		Optional<Kle> parentMaybe = Optional.empty();
		if(parts.length == 2){
			parentMaybe = mainMaybe;
		} else if(parts.length == 3) {
			parentMaybe = kleService.fetchMainGroup(main + "."+parts[1], kle.getMunicipalityId());
			if(!parentMaybe.isPresent()){
				throw new ValidationException("Der blev ikke fundet en gruppe for kle. Opret gruppe først.");
			}
		}
		return parentMaybe.get();
	}
}
