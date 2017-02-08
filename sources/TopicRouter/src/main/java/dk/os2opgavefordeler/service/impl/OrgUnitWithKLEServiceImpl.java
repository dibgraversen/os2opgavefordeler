package dk.os2opgavefordeler.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;

import dk.os2opgavefordeler.auth.AuthService;
import dk.os2opgavefordeler.model.Kle;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.OrgUnit_;
import dk.os2opgavefordeler.model.presentation.KleAssignmentType;
import dk.os2opgavefordeler.model.presentation.OrgUnitWithKLEPO;
import dk.os2opgavefordeler.repository.UserRepository;
import dk.os2opgavefordeler.service.OrgUnitWithKLEService;
import dk.os2opgavefordeler.service.PersistenceService;

@ApplicationScoped
public class OrgUnitWithKLEServiceImpl implements OrgUnitWithKLEService {

	@Inject
	private Logger logger;

	@Inject
	private PersistenceService persistence;

	@Inject
	private UserRepository userRepository;

	@Inject
	private AuthService authService;

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
				result.add(new OrgUnitWithKLEPO(orgUnit.getId(), orgUnit.getName(),
						orgUnit.getParent().isPresent() ? orgUnit.getParent().get().getName() : null));
			}
		} catch (NoResultException nre) {
			; // we just return the empty list
		}

		return result;
	}

	@Override
	public OrgUnitWithKLEPO get(long id) {
		final List<OrgUnit> results = persistence.criteriaFind(OrgUnit.class,
				(cb, cq, ou) -> cq.where(cb.equal(ou.get(OrgUnit_.id), id)));
		if (!results.isEmpty()) {
			OrgUnit ouEntity = results.get(0);
			System.out.println("KLE: " + ouEntity.toString());
			OrgUnitWithKLEPO ou = new OrgUnitWithKLEPO(ouEntity.getId(), ouEntity.getName(),
					ouEntity.getParent().isPresent() ? ouEntity.getParent().get().getName() : null);
			ou.setInterestKLE(ouEntity.getKles().stream().map(Kle::getNumber).collect(Collectors.toList()));
			return ou;
		} else {
			return null;
		}
	}

	@Override
	public OrgUnitWithKLEPO addKLE(long id, String kleNumber, KleAssignmentType assignmentType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrgUnitWithKLEPO removeKLE(long id, String kleNumber, KleAssignmentType assignmentType) {
		// TODO Auto-generated method stub
		return null;
	}

}
