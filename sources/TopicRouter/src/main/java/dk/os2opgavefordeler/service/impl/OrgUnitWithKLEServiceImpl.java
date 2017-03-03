package dk.os2opgavefordeler.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;

import dk.os2opgavefordeler.auth.AuthService;
import dk.os2opgavefordeler.model.Kle;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.OrgUnit_;
import dk.os2opgavefordeler.model.presentation.KleAssignmentType;
import dk.os2opgavefordeler.model.presentation.OrgUnitListPO;
import dk.os2opgavefordeler.model.presentation.OrgUnitWithKLEPO;
import dk.os2opgavefordeler.repository.UserRepository;
import dk.os2opgavefordeler.service.KleService;
import dk.os2opgavefordeler.service.OrgUnitWithKLEService;
import dk.os2opgavefordeler.service.PersistenceService;

@ApplicationScoped
public class OrgUnitWithKLEServiceImpl implements OrgUnitWithKLEService {

	@Inject
	private Logger log;

	@Inject
	private PersistenceService persistence;

	@Inject
	private UserRepository userRepository;

	@Inject
	private AuthService authService;

	@Inject
	private KleService kleService;
	
	@Override
	public List<OrgUnitListPO> getList(long municipalityId) {
		TypedQuery<OrgUnit> query = persistence.getEm().createQuery(
				"SELECT org FROM OrgUnit org WHERE org.municipality.id = :municipalityId AND org.isActive = true",
				OrgUnit.class);

		query.setParameter("municipalityId", municipalityId);
		List<OrgUnitListPO> result = new ArrayList<>();

		try {
			final List<OrgUnit> orgUnits = query.getResultList();
			for (OrgUnit orgUnit : orgUnits) {
				result.add(new OrgUnitListPO(orgUnit.getId(), orgUnit.getName(), (orgUnit.getParent().isPresent() ? orgUnit.getParent().get().getName() : null), orgUnit.hasKles()));
			}
		} catch (NoResultException nre) {
			; // we just return the empty list
		}

		return result;
	}

	@Override
	public List<OrgUnitWithKLEPO> getAll(long municipalityId) {
		TypedQuery<OrgUnit> query = persistence.getEm().createQuery(
				"SELECT org FROM OrgUnit org WHERE org.municipality.id = :municipalityId AND org.isActive = true",
				OrgUnit.class);

		query.setParameter("municipalityId", municipalityId);
		List<OrgUnitWithKLEPO> result = new ArrayList<>();

		try {
			final List<OrgUnit> orgUnits = query.getResultList();
			for (OrgUnit orgUnit : orgUnits) {
				result.add(new OrgUnitWithKLEPO(orgUnit.getId(), orgUnit.getName()));
			}
		} catch (NoResultException nre) {
			; // we just return the empty list
		}

		return result;
	}

	@Override
	public OrgUnitWithKLEPO get(long id, Municipality municipality) {
		final List<OrgUnit> results = persistence.criteriaFind(OrgUnit.class,
				(cb, cq, ou) -> cq.where(cb.and(cb.equal(ou.get(OrgUnit_.id), id)),cb.equal(ou.get(OrgUnit_.municipality), municipality)));

		if (!results.isEmpty()) {
			OrgUnit ouEntity = results.get(0);

			return buildOrgUnitWithKLEPO(ouEntity);
		}

		return null;
	}

	private OrgUnitWithKLEPO buildOrgUnitWithKLEPO(OrgUnit ouEntity) {
		OrgUnitWithKLEPO ou = new OrgUnitWithKLEPO(ouEntity.getId(), ouEntity.getName());
		ou.setInterestKLE(ouEntity.getKles(KleAssignmentType.INTEREST).stream().map(Kle::getNumber).collect(Collectors.toList()));
		ou.setPerformingKLE(ouEntity.getKles(KleAssignmentType.PERFORMING).stream().map(Kle::getNumber).collect(Collectors.toList()));

		return ou;
	}

	@Override
	public boolean addKLE(long ouId, String kleNumber, KleAssignmentType assignmentType) {
		final List<OrgUnit> results = persistence.criteriaFind(OrgUnit.class,
				(cb, cq, ou) -> cq.where(cb.equal(ou.get(OrgUnit_.id), ouId)));

		boolean success = false;

		if (!results.isEmpty()) {
			OrgUnit orgUnit = results.get(0);
			try {
				Kle kle = kleService.getKle(kleNumber);
				orgUnit.addKle(kle, assignmentType);
				persistence.getEm().persist(orgUnit);
				success = true;
			} catch(PersistenceException ex) {
				log.error("An error occured while adding KLE to OrgUnit.",ex);
				success = false;
			}
		}

		return success;
	}

	@Override
	public boolean removeKLE(long ouId, String kleNumber, KleAssignmentType assignmentType) {
		final List<OrgUnit> results = persistence.criteriaFind(OrgUnit.class,
				(cb, cq, ou) -> cq.where(cb.equal(ou.get(OrgUnit_.id), ouId)));

		boolean success = false;

		if (!results.isEmpty()) {
			OrgUnit orgUnit = results.get(0);
			try {
				Kle kle = kleService.getKle(kleNumber);
				orgUnit.removeKle(kle, assignmentType);
				persistence.getEm().persist(orgUnit);
				success = true;
			} catch (PersistenceException ex) {
				log.error("An error occured while removing KLE from OrgUnit.",ex);
				success = false;
			}
		}

		return success;
	}

	@Override
	public boolean containsKLE(OrgUnit ou, KleAssignmentType assignmentType, String kleNumber) {
		return ou.getKles(assignmentType).stream().anyMatch(kle->kle.getNumber().equals(kleNumber));
	}
}
