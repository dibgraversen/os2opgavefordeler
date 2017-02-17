package dk.os2opgavefordeler.service.impl;

import dk.os2opgavefordeler.model.*;
import dk.os2opgavefordeler.model.presentation.OrgUnitPO;
import dk.os2opgavefordeler.service.OrgUnitService;
import dk.os2opgavefordeler.service.PersistenceService;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class OrgUnitServiceImpl implements OrgUnitService {

	@Inject
	Logger logger;

	@Inject
	PersistenceService persistence;

	private OrgUnit saveOrgUnit(OrgUnit orgUnit) {
		EntityManager em = persistence.getEm();

		Municipality currentMunicipality = orgUnit.getMunicipality().get();

		logger.info("[saveOrgUnit] Municipality: {}", currentMunicipality);

		List<Employment> newEmployments = new ArrayList<>();

		// check for existence.
		Optional<OrgUnit> orgLookup = getOrgUnitFromBusinessKey(orgUnit.getBusinessKey(), orgUnit.getMunicipality().get().getId());
		boolean updating = orgLookup.isPresent();

		OrgUnit result = null;

		if (updating) {
			result = orgLookup.get();
		}
		else {
			result = orgUnit;
		}

		if (updating) {
			result.setName(orgUnit.getName());
			result.setEmail(orgUnit.getEmail());
			result.setEsdhId(orgUnit.getEsdhId());
			result.setEsdhLabel(orgUnit.getEsdhLabel());
			result.setPhone(orgUnit.getPhone());
		}

		// fix parent reference.
		Optional<OrgUnit> givenParent = orgUnit.getParent();

		if (givenParent.isPresent()) {
			logger.info("[saveOrgUnit] Found parent");

			Optional<OrgUnit> parent = getOrgUnitFromBusinessKey(givenParent.get().getBusinessKey(), currentMunicipality.getId());

			if (parent.isPresent()) {
				logger.info("[saveOrgUnit] Fixing parent for OrgUnit " + orgUnit.getName() + " - settting to " + parent.get().getName());
				orgUnit.setParent(parent.get());
			}
			else {
				logger.error("[saveOrgUnit] Given parent set - but no parent found using business key"); // TODO: Create one???
			}
		}
		else {
			logger.info("[saveOrgUnit] No parent found");
		}

		// fix up manager.
		// TODO check if collection is deprecated.
		List<Employment> newEmployeesCollection = new ArrayList<>();

		Optional<Employment> givenManager = orgUnit.getManager();

		if (givenManager.isPresent()) {
			Optional<Employment> manager = getEmploymentFromBusinessKey(givenManager.get().getBusinessKey(), currentMunicipality.getId());

			if (manager.isPresent()) {
				Employment managerEntity = manager.get();
				updateEmployment(givenManager.get(), managerEntity);
				result.setManager(managerEntity);

				if (updating) {
					managerEntity.setEmployedIn(result);
				}
				else {
					managerEntity.setEmployedIn(null);
					newEmployments.add(managerEntity);
				}

				em.merge(managerEntity);
			}
			else {
				Employment newManager = givenManager.get();

				newManager.setEmployedIn(null);

				if (newManager.getMunicipality() == null) {
					newManager.setMunicipality(currentMunicipality);
				}

				em.persist(newManager);
				newEmployments.add(newManager);
			}
		}
		else if (updating) {
			result.setManager(null);
		}

		// employees
		if (result.getEmployees() != null && !result.getEmployees().isEmpty()) {
			// remove by setting employed in = blank if no longer part of employees.
			if(orgUnit.getEmployees() != null && !orgUnit.getEmployees().isEmpty()){
				for (Employment employment : result.getEmployees()) {
					boolean found = false;

					for (Employment newEmployment : orgUnit.getEmployees()) {
						if (result.getManager().isPresent()){
							if (employment.getBusinessKey().equals(newEmployment.getBusinessKey()) ||
									employment.getBusinessKey().equals(result.getManager().get().getBusinessKey())){
								found = true;
							}
						}
					}

					if (!found){
						Query query = em.createQuery("UPDATE Employment e SET e.employedIn = NULL WHERE e.id = :employmentId");
						query.setParameter("employmentId", employment.getId());
						query.executeUpdate();
					}
				}
			}
		}

		if (orgUnit.getEmployees() != null && !orgUnit.getEmployees().isEmpty()){
			for (Employment employment : orgUnit.getEmployees()) {
				Optional<Employment> employmentLookup = getEmploymentFromBusinessKey(employment.getBusinessKey(), currentMunicipality.getId());

				if (employmentLookup.isPresent()){
					logger.info("employment found.");

					Employment existingEmployment = employmentLookup.get();
					updateEmployment(employment, existingEmployment);

					if (updating) {
						existingEmployment.setEmployedIn(result);
					}
					else {
						existingEmployment.setEmployedIn(null);
						newEmployments.add(existingEmployment);
					}

					em.merge(existingEmployment);
					newEmployeesCollection.add(existingEmployment);
				}
				else {
					logger.info("creating new");
					if (updating) {
						employment.setEmployedIn(result);
					}
					else {
						employment.setEmployedIn(null);
						newEmployments.add(employment);
					}
					employment.setMunicipality(currentMunicipality);
					logger.info("employment: {}", employment);
					em.persist(employment);
					newEmployeesCollection.add(employment);
					newEmployments.add(employment);
				}
			}

			if (updating){
				result.setEmployees(newEmployeesCollection);
			}
		}

		if (updating) {
			em.merge(result);
		}
		else {
			em.persist(result);

			// now that we've saved this, set employed in on employments.
			for (Employment newEmployment : newEmployments) {
				newEmployment.setEmployedIn(result);
				em.merge(newEmployment);
			}
		}

		return result;
	}

	private void updateEmployment(Employment from, Employment to){
		to.setIsActive(from.isActive());
		to.setName(from.getName());
		to.setEmail(from.getEmail());
		to.setEsdhId(from.getEsdhId());
		to.setPhone(from.getPhone());
		to.setInitials(from.getInitials());
		to.setJobTitle(from.getJobTitle());
	}

	private Optional<OrgUnit> getOrgUnitFromBusinessKey(String businessKey, long municipalityId){
		Query query = persistence.getEm().createQuery("SELECT org FROM OrgUnit org WHERE org.businessKey = :businessKey AND org.municipality.id = :municipalityId");
		query.setParameter("businessKey", businessKey);
		query.setParameter("municipalityId", municipalityId);
		try {
			OrgUnit result = (OrgUnit) query.getSingleResult();
			return Optional.of(result);
		} catch(NoResultException nre){
			return Optional.empty();
		}
	}

	private Optional<Employment> getEmploymentFromBusinessKey(String businessKey, long municipalityId){
		Query query = persistence.getEm().createQuery("SELECT emp FROM Employment emp WHERE emp.businessKey = :businessKey AND emp.municipality.id = :municipalityId");
		query.setParameter("businessKey", businessKey);
		query.setParameter("municipalityId", municipalityId);
		try{
			Employment employment = (Employment) query.getSingleResult();
			return Optional.of(employment);
		} catch	(Exception e){
			return Optional.empty();
		}
	}

	private List<String> getChildBusinessKeys(OrgUnit root){
		List<String> result = new ArrayList<>();
		result.add(root.getBusinessKey());
		if(root.getChildren() != null && !root.getChildren().isEmpty()){
			for (OrgUnit orgUnit : root.getChildren()) {
				result.addAll(getChildBusinessKeys(orgUnit));
			}
		}
		return result;
	}

	private void deleteNotInCollection(List<String> businessKeys, long municipalityId){
		List<Long> orgIdsForDeletion = getOrgIdsForDeletion(businessKeys, municipalityId);
		if(orgIdsForDeletion.size() > 0){
			unlinkAssignedOrgs(orgIdsForDeletion);
			unlinkResponsible(orgIdsForDeletion);
			unlinkEmployments(orgIdsForDeletion);
			deleteOrgs(orgIdsForDeletion);
		}
	}

	@SuppressWarnings("unchecked")
	private List<Long> getOrgIdsForDeletion(List<String> businessKeys, long municipalityId){
		EntityManager em = persistence.getEm();
		Query getOrgIdsForDeletion = em.createQuery("SELECT org.id FROM OrgUnit org WHERE org.businessKey NOT IN (:businessKeys) AND org.municipality.id = :municipalityId");
		getOrgIdsForDeletion.setParameter("businessKeys", businessKeys);
		getOrgIdsForDeletion.setParameter("municipalityId", municipalityId);
		return getOrgIdsForDeletion.getResultList();
	}

	@SuppressWarnings("unchecked")
	private void unlinkAssignedOrgs(List<Long> orgIds){
		EntityManager em = persistence.getEm();
		Query assignedRuleIdsQuery = em.createQuery("SELECT rule.id FROM DistributionRule rule LEFT JOIN rule.assignedOrg as org WHERE rule.assignedOrg.id = org.id AND org.id IN (:orgIds)");
		assignedRuleIdsQuery.setParameter("orgIds", orgIds);
		List<Long> assignedRuleIds = (List<Long>)assignedRuleIdsQuery.getResultList();
		if(assignedRuleIds.size() > 0){
			Query unrefAssignedOrg = em.createQuery("UPDATE DistributionRule rule SET rule.assignedOrg = NULL WHERE rule.id IN (:assignedRuleIds)");
			unrefAssignedOrg.setParameter("assignedRuleIds", assignedRuleIds);
			unrefAssignedOrg.executeUpdate();
		}
	}

	@SuppressWarnings("unchecked")
	private void unlinkResponsible(List<Long> orgIds){
		EntityManager em = persistence.getEm();
		Query responsibleRuleIdsQuery = em.createQuery("SELECT rule.id FROM DistributionRule rule LEFT JOIN rule.responsibleOrg as org WHERE rule.responsibleOrg.id = org.id AND org.id IN (:orgIds)");
		responsibleRuleIdsQuery.setParameter("orgIds", orgIds);
		List<Long> responsibleRuleIds = (List<Long>) responsibleRuleIdsQuery.getResultList();
		if(responsibleRuleIds.size() > 0){
			Query unrefAssignedOrg = em.createQuery("UPDATE DistributionRule rule SET rule.responsibleOrg = NULL WHERE rule.id IN (:responsibleRuleIds)");
			unrefAssignedOrg.setParameter("responsibleRuleIds", responsibleRuleIds);
			unrefAssignedOrg.executeUpdate();
		}
	}

	private void unlinkEmployments(List<Long> orgIds){
		EntityManager em = persistence.getEm();
		Query unlinkEmploymentsQuery = em.createQuery("UPDATE Employment e SET e.employedIn = null WHERE e.employedIn.id IN (:orgIds)");
		unlinkEmploymentsQuery.setParameter("orgIds", orgIds);
		unlinkEmploymentsQuery.executeUpdate();
	}

	private void deleteOrgs(List<Long> orgIds){
		EntityManager em = persistence.getEm();
		Query unlinkEmploymentsQuery = em.createQuery("DELETE FROM OrgUnit org WHERE org.id IN (:orgIds)");
		unlinkEmploymentsQuery.setParameter("orgIds", orgIds);
		unlinkEmploymentsQuery.executeUpdate();
	}

	@Override
	public void importOrganization(OrgUnit orgUnit) {
		// TODO MUY IMPORTANTE! The given orgUnit must be top level orgUnit.
		if(!orgUnit.getMunicipality().isPresent()){
			// return invalid params exception
		}
		Municipality currentMunicipality = orgUnit.getMunicipality().get();
		fixRelations(orgUnit);

		Optional<OrgUnit> existing = getOrgUnitFromBusinessKey(orgUnit.getBusinessKey(), currentMunicipality.getId());
		if (existing.isPresent()){
			// do update
			List<String> businessKeys = getChildBusinessKeys(orgUnit);
			if(businessKeys != null && !businessKeys.isEmpty()){
				deleteNotInCollection(businessKeys, currentMunicipality.getId());
			}
		}
		saveOrgUnitWithChildren(orgUnit);
	}

	private void saveOrgUnitWithChildren(OrgUnit org){
		saveOrgUnit(org);
		if(org.getChildren() != null && !org.getChildren().isEmpty()){
			for (OrgUnit orgUnit : org.getChildren()) {
				saveOrgUnitWithChildren(orgUnit);
			}
		}
	}

	@Override
	public Optional<OrgUnit> getOrgUnit(long id) {
		final List<OrgUnit> results = persistence.criteriaFind(OrgUnit.class,
				(cb, cq, ou) -> cq.where(cb.equal(ou.get(OrgUnit_.id), id)
				)
		);

		return results.isEmpty() ?
			Optional.empty() :
			Optional.of(results.get(0));
	}
	

	@Override
	public Optional<OrgUnit> getOrgUnit(long id, Municipality municipality) {
		final List<OrgUnit> results = persistence.criteriaFind(OrgUnit.class,
				(cb, cq, ou) -> cq.where(
						cb.and(
								cb.equal(ou.get(OrgUnit_.id), id)),
								cb.equal(ou.get(OrgUnit_.municipality), municipality)
								)
				);

		return results.isEmpty() ?
			Optional.empty() :
			Optional.of(results.get(0));
	}

	@Override
	@SuppressWarnings("unchecked")
	public Optional<OrgUnit> getToplevelOrgUnit(long municipalityId) {
		TypedQuery<OrgUnit> query = persistence.getEm().createQuery("SELECT org FROM OrgUnit org WHERE org.parent IS NULL AND org.municipality.id = :municipalityId AND org.isActive = true", OrgUnit.class);
		query.setParameter("municipalityId", municipalityId);
		Optional<OrgUnit> result;
		try {
			final OrgUnit orgUnit = query.getSingleResult();
			orgUnit.getEmployees();
			touchChildren(orgUnit.getChildren());
			result = Optional.of(orgUnit);
		} catch (NoResultException nre){
			result = Optional.empty();
		}
		return result;
	}

	private Optional<OrgUnit> getManagedOrgUnit(long municipalityId, long employmentId){
		TypedQuery<OrgUnit> query = persistence.getEm().createQuery("SELECT org FROM OrgUnit org WHERE org.manager.id = :managerId AND org.municipality.id = :municipalityId", OrgUnit.class);
		query.setParameter("managerId", employmentId);
		query.setParameter("municipalityId", municipalityId);
		Optional<OrgUnit> result;
		try {
			final OrgUnit orgUnit = query.getSingleResult();
			orgUnit.getEmployees();
			touchChildren(orgUnit.getChildren());
			result = Optional.of(orgUnit);
		} catch (NoResultException nre){
			result = Optional.empty();
		}
		return result;
	}

	@Override
	public List<OrgUnit> getManagedOrgUnits(long municipalityId, long employmentId){
		final Optional<OrgUnit> managedOrgUnit = getManagedOrgUnit(municipalityId, employmentId);
		return managedOrgUnit.map( ou -> ou.flattened().collect(Collectors.toList()) )
				.orElse(Collections.emptyList());
	}

	@Override
	public List<OrgUnitPO> getManagedOrgUnitsPO(long municipalityId, long employmentId){
		final Optional<OrgUnit> managedOrgUnit = getManagedOrgUnit(municipalityId, employmentId);
		return managedOrgUnit.map( ou -> ou.flattened().map(OrgUnitPO::new).collect(Collectors.toList()) )
				.orElse(Collections.emptyList());
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<OrgUnit> findByName(String name) {
		final List<OrgUnit> results = persistence.criteriaFind(OrgUnit.class,
			(cb, cq, ou) -> cq.where( cb.like(ou.get(OrgUnit_.name), name))
		);
		return results;
	}

//	public List<Employment> getSubordinateManagers(OrgUnit ou) {
//		return ou.flattened().map(OrgUnit::getManager).collect(Collectors.toList());
//	}


	@Override
	public List<OrgUnitPO> getToplevelOrgUnitPO(long municipalityId) {
		final Optional<OrgUnit> orgUnit = getToplevelOrgUnit(municipalityId);

		return orgUnit.map( ou -> ou.flattened().map(OrgUnitPO::new).collect(Collectors.toList()) )
			.orElse(Collections.emptyList());
	}

	@Override
	public Optional<OrgUnitPO> getOrgUnitPO(long id) {
		return getOrgUnit(id).map(OrgUnitPO::new);
	}

	@Override
	public Optional<Employment> getEmployment(long id) {
		final List<Employment> results = persistence.criteriaFind(Employment.class,
			(cb, cq, ou) -> cq.where(cb.equal(ou.get(Employment_.id), id))
		);

		return results.isEmpty() ?
			Optional.empty() :
			Optional.of(results.get(0));
	}

	@Override
	public Optional<Employment> getEmploymentByName(long municipalityId,  String name) {
		Query query = persistence.getEm().createQuery("SELECT e FROM Employment e WHERE e.name = :name AND e.municipality.id = :municipalityId");
		query.setParameter("name", name);
		query.setParameter("municipalityId", municipalityId);
		final List<Employment> results = query.getResultList();

		return results.isEmpty() ?
			Optional.empty() :
			Optional.of(results.get(0));
	}

	private List<OrgUnit> touchChildren(List<OrgUnit> ou) {
		ou.forEach(child -> {
			child.getEmployees().size();
			child.getMunicipality();
			touchChildren(child.getChildren());
		});
		return ou;
	}

	/**
	 * This method sets parents so that relations go both ways.
	 * @param input an org node to fix - it's children will have current node as parent.
	 */
	private void fixRelations(OrgUnit input) {
		input.getMunicipality().ifPresent(mun -> input.setMunicipality(findMunicipalityByName(mun.getName())));
		input.getChildren().forEach(child -> {
			child.setParent(input);
			fixRelations(child);
		});
	}

	public Optional<Employment> findResponsibleManager(OrgUnit orgUnit){
		if(orgUnit.getManager().isPresent()){
			return orgUnit.getManager();
		} else {
			if (orgUnit.getParent().isPresent()){
				return findResponsibleManager(orgUnit.getParent().get());
			}
		}
		logger.warn("Found OrgUnit parent without manager: {}", orgUnit);
		return Optional.empty();
	}

	public Optional<Employment> getActualManager(Long orgId){
		Optional<OrgUnit> orgMaybe = getOrgUnit(orgId);
		if(orgMaybe.isPresent()){
			OrgUnit org = orgMaybe.get();
			if(org.getManager().isPresent()){
				return org.getManager();
			} else if(org.getParent().isPresent()){
				return getActualManager(org.getParent().get().getId());
			}
		}
		return Optional.empty();
	}

	private Municipality findMunicipalityByName(String name){
		Query query = persistence.getEm().createQuery("SELECT m FROM Municipality m WHERE m.name = :name");
		query.setParameter("name", name);
		return (Municipality) query.getSingleResult();
	}

	@Override
	public Optional<OrgUnit> findByBusinessKeyAndMunicipality(String businessKey, Municipality municipality) {
		final List<OrgUnit> results = persistence.criteriaFind(OrgUnit.class,
				(cb, cq, ou) -> cq.where(
						cb.and(
								cb.equal(ou.get(OrgUnit_.businessKey), businessKey),
								cb.equal(ou.get(OrgUnit_.municipality), municipality)
						)
				)
		);

		return results.isEmpty() ?
			Optional.empty() :
			Optional.of(results.get(0));
	}

}
