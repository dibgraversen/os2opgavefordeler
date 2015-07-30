package dk.os2opgavefordeler.service.impl;

import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Employment_;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.OrgUnit_;
import dk.os2opgavefordeler.model.presentation.OrgUnitPO;
import dk.os2opgavefordeler.service.MunicipalityService;
import dk.os2opgavefordeler.service.OrgUnitService;
import dk.os2opgavefordeler.service.PersistenceService;
import org.slf4j.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class OrgUnitServiceImpl implements OrgUnitService {
	@Inject
	Logger logger;

	@Inject
	PersistenceService persistence;

	@Inject
	MunicipalityService municipalityService;

	@Override
	public OrgUnit saveOrgUnit(OrgUnit orgUnit) {
		EntityManager em = persistence.getEm();
		Long currentMunicipality = orgUnit.getMunicipality().get().getId();
		List<Employment> newEmployments = new ArrayList<>();

		// check for existence.
		Optional<OrgUnit> orgLookup = getOrgUnitFromBusinessKey(orgUnit.getBusinessKey(), orgUnit.getMunicipality().get().getId());
		boolean updating = orgLookup.isPresent();
		OrgUnit result = null;
		if (updating) {
			result = orgLookup.get();
		} else {
			result = orgUnit;
		}
		if(updating){
			result.setName(orgUnit.getName());
			result.setEmail(orgUnit.getEmail());
			result.setEsdhId(orgUnit.getEsdhId());
			result.setEsdhLabel(orgUnit.getEsdhLabel());
			result.setPhone(orgUnit.getPhone());
		}

		// fix parent reference.
		Optional<OrgUnit> givenParent = orgUnit.getParent();
		if (givenParent.isPresent()){
			Optional<OrgUnit> parent = getOrgUnitFromBusinessKey(givenParent.get().getBusinessKey(), currentMunicipality);
			if (parent.isPresent()){
				orgUnit.setParent(parent.get());
			} else {
				logger.error("no parent found");
				// TODO create one???
			}
		}

		// fix up manager.
		Optional<Employment> givenManager = orgUnit.getManager();
		if(givenManager.isPresent()){
			Optional<Employment> manager = getEmploymentFromBusinessKey(givenManager.get().getBusinessKey(), currentMunicipality);
			if(manager.isPresent()){
				// TODO make sure changes are handled.
				orgUnit.setManager(manager.get());
			} else {
				em.persist(givenManager.get());
				newEmployments.add(givenManager.get());
			}
		}

		// employees
		if(result.getEmployees() != null && !result.getEmployees().isEmpty()){
			List<Employment> newEmployees = new ArrayList<>();
			for (Employment employment : result.getEmployees()) {
				Optional<Employment> employmentLookup = getEmploymentFromBusinessKey(employment.getBusinessKey(), currentMunicipality);
				if(employmentLookup.isPresent()){
					Employment existingEmployment = employmentLookup.get();
					existingEmployment.setIsActive(employment.isActive());
					existingEmployment.setName(employment.getName());
					existingEmployment.setEmail(employment.getEmail());
					existingEmployment.setEsdhId(employment.getEsdhId());
					existingEmployment.setPhone(employment.getPhone());
					existingEmployment.setInitials(employment.getInitials());
					existingEmployment.setJobTitle(employment.getJobTitle());
					em.merge(existingEmployment);
				} else {
					employment.setEmployedIn(null);
					em.persist(employment);
					newEmployments.add(employment);
				}
			}
			result.setEmployees(newEmployees);
		}

		if(updating){
			em.merge(result);
		} else {
			em.persist(result);
			// now that we've saved this, set employed in on employments.
			for (Employment newEmployment : newEmployments) {
				newEmployment.setEmployedIn(result);
				em.merge(newEmployment);
			}
		}
		return result;
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
		Query query = persistence.getEm().createQuery("SELECT emp FROM Employment emp WHERE emp.businessKey = :businessKey AND emp.employedIn.municipality.id = :municipalityId");
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
		unlinkAssignedOrgs(orgIdsForDeletion);
		unlinkResponsible(orgIdsForDeletion);
		unlinkEmployments(orgIdsForDeletion);
		deleteOrgs(orgIdsForDeletion);
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
		Query unrefAssignedOrg = em.createQuery("UPDATE DistributionRule rule SET rule.assignedOrg = NULL WHERE rule.id IN (:assignedRuleIds)");
		unrefAssignedOrg.setParameter("assignedRuleIds", assignedRuleIds);
		unrefAssignedOrg.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	private void unlinkResponsible(List<Long> orgIds){
		EntityManager em = persistence.getEm();
		Query responsibleRuleIdsQuery = em.createQuery("SELECT rule.id FROM DistributionRule rule LEFT JOIN rule.responsibleOrg as org WHERE rule.responsibleOrg.id = org.id AND org.id IN (:orgIds)");
		responsibleRuleIdsQuery.setParameter("orgIds", orgIds);
		List<Long> responsibleRuleIds = (List<Long>) responsibleRuleIdsQuery.getResultList();
		Query unrefAssignedOrg = em.createQuery("UPDATE DistributionRule rule SET rule.responsibleOrg = NULL WHERE rule.id IN (:responsibleRuleIds)");
		unrefAssignedOrg.setParameter("responsibleRuleIds", responsibleRuleIds);
		unrefAssignedOrg.executeUpdate();
	}

	public void unlinkEmployments(List<Long> orgIds){
		EntityManager em = persistence.getEm();
		Query unlinkEmploymentsQuery = em.createQuery("UPDATE Employment e SET e.employedIn = null WHERE e.employedIn.id IN (:orgIds)");
		unlinkEmploymentsQuery.setParameter("orgIds", orgIds);
		unlinkEmploymentsQuery.executeUpdate();
	}

	public void deleteOrgs(List<Long> orgIds){
		EntityManager em = persistence.getEm();
		Query unlinkEmploymentsQuery = em.createQuery("DELETE OrgUnit org WHERE org.id IN (:orgIds)");
		unlinkEmploymentsQuery.setParameter("orgIds", orgIds);
		unlinkEmploymentsQuery.executeUpdate();
	}

	@Override
	public void importOrganization(OrgUnit orgUnit) {
		// TODO MUY IMPORTANTE! The given orgUnit must bee top level orgUnit.
		logger.warn("in #createOrgUnit with orgUnit: "+orgUnit);
		fixRelations(orgUnit);
		Municipality currentMunicipality = orgUnit.getMunicipality().get();
		Optional<OrgUnit> existing = getOrgUnitFromBusinessKey(orgUnit.getBusinessKey(), currentMunicipality.getId());
		if(existing.isPresent()){
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
	@SuppressWarnings("unchecked")
	public Optional<OrgUnit> getToplevelOrgUnit(long municipalityId) {
		Query query = persistence.getEm().createQuery("SELECT org FROM OrgUnit org WHERE org.parent IS NULL AND org.municipality.id = :municipalityId");
		query.setParameter("municipalityId", municipalityId);
		Optional<OrgUnit> result;
		try {
			OrgUnit orgUnit = (OrgUnit) query.getSingleResult();
			result = Optional.of(orgUnit);
		} catch (NoResultException nre){
			result = Optional.empty();
		}
		return result;
	}

	@Override
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
		final List<Employment> results = persistence.criteriaFind(Employment.class,
			(cb, cq, ou) -> cq.where(cb.equal(ou.get(Employment_.name), name))
		);

		return results.isEmpty() ?
			Optional.empty() :
			Optional.of(results.get(0));
	}

	private List<OrgUnit> touchChildren(List<OrgUnit> ou) {
		ou.forEach(child -> {
			child.getEmployees().size();
			touchChildren(child.getChildren());
		});
		return ou;
	}

	private void fixRelations(OrgUnit input) {
		input.getMunicipality().ifPresent(mun -> input.setMunicipality(municipalityService.findByName(mun.getName())));
		input.getChildren().forEach(child -> {
			child.setParent(input);
			fixRelations(child);
		});
	}
}
