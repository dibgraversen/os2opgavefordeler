package dk.os2opgavefordeler.service.impl;

import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Employment_;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.presentation.EmploymentPO;
import dk.os2opgavefordeler.model.search.EmploymentSearch;
import dk.os2opgavefordeler.model.search.SearchResult;
import dk.os2opgavefordeler.service.EmploymentService;
import dk.os2opgavefordeler.service.OrgUnitService;
import dk.os2opgavefordeler.service.PersistenceService;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class EmploymentServiceImpl implements EmploymentService {
	public static final int MAX_RESULTS = 20;

	@Inject
	PersistenceService persistence;

	@Inject
	OrgUnitService orgUnitService;

	@Inject
	Logger log;

	EntityManager em;

	@Override
	public Optional<Employment> getEmployment(Long id) {
		if(id == 0L){
			return Optional.empty();
		}
		final List<Employment> results = persistence.criteriaFind(Employment.class,
			(cb, cq, ou) -> cq.where(cb.equal(ou.get(Employment_.id), id)
			)
		);

		return results.isEmpty() ?
			Optional.empty() :
			Optional.of(results.get(0));
	}

	@Override
	public List<Employment> findByEmail(String email) {
		final List<Employment> results = persistence.criteriaFind(Employment.class,
				(cb, cq, ou) -> cq.where(cb.equal(ou.get(Employment_.email), email)
				)
		);

		return results;
	}

	@Override
	public Optional<EmploymentPO> getEmploymentPO(Long id) {
		return getEmployment(id).map(EmploymentPO::new);
	}

	private List<Employment> getManaged(long municipalityId, long employmentId){
		Map<Long, Employment> resultMap = new HashMap<>();
		Optional<Employment> managerMaybe = getEmployment(employmentId);
		if(managerMaybe.isPresent()){
			Employment manager = managerMaybe.get();
			List<OrgUnit> managedOrgunits = new ArrayList<>();
			OrgUnit top = manager.getEmployedIn();
			managedOrgunits.add(top);
			List<OrgUnit> managedOrgs = orgUnitService.getManagedOrgUnits(municipalityId, employmentId);
			managedOrgunits.addAll(managedOrgs);
			for (OrgUnit managedOrgunit : managedOrgunits) {
				// Add manager.
				if(managedOrgunit.getManager().isPresent()){
					Employment currentManager = managedOrgunit.getManager().get();
					if(!resultMap.containsKey(currentManager.getId())) {
						resultMap.put(currentManager.getId(), currentManager);
					}
				}
				// Add employees.
				if(!managedOrgunit.getChildren().isEmpty()){
					for (Employment employee : managedOrgunit.getEmployees()) {
						if (!resultMap.containsKey(employee.getId())) {
							resultMap.put(employee.getId(), employee);
						}
					}
				}
			}
		}
		return new ArrayList<>(resultMap.values());
	}

	@Override
	public List<EmploymentPO> getManagedAsPO(long municipalityId, long employmentId){
		final List<Employment> employments = getManaged(municipalityId, employmentId);
		return employments.stream()
				.map(emp -> {
					EmploymentPO newEmp = new EmploymentPO(emp);
					newEmp.setSubordinate(true);
					return newEmp;
				})
				.limit(MAX_RESULTS)
				.sorted( (e1, e2) -> e1.getName().compareTo(e2.getName()) )
				.collect(Collectors.toList());
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Employment> getAll(long municipalityId) {
		Query query = persistence.getEm().createQuery("SELECT emp FROM Employment emp WHERE emp.employedIn.municipality.id = :municipalityId ORDER BY emp.name ASC");
		query.setMaxResults(MAX_RESULTS);
		query.setParameter("municipalityId", municipalityId);
		return query.getResultList();
	}

	@Override
	public List<EmploymentPO> getAllPO(long municipalityId, long employmentId) {
		final List<Employment> employments = getAll(municipalityId);
		final List<Employment> managed = getManaged(municipalityId, employmentId);
		List<EmploymentPO> result = employments.stream()
				.map(emp -> {
					EmploymentPO newEmp = new EmploymentPO(emp);
					newEmp.setSubordinate(managed.contains(emp));
					return newEmp;
				})
				.collect(Collectors.toList());

		return result;
	}

	@SuppressWarnings("unchecked")
	public SearchResult<EmploymentPO> search(EmploymentSearch search){
		SearchResult<EmploymentPO> result = new SearchResult();
		Municipality municipality = getMunicipality(search.getMunicipalityId());
		result.setTotalMatches(getSearchCount(search, municipality));
		result.setResults(getSearchResults(search, municipality));
		return result;
	}

	@Override
	public List<EmploymentPO> getSubordinates(Long employmentId) {
		List<EmploymentPO> result = new ArrayList<>();
		Optional<Employment> employmentMaybe = getEmployment(employmentId);
		if(employmentMaybe.isPresent()){
			Employment employment = employmentMaybe.get();
			List<OrgUnit> managedOrgUnits = orgUnitService.getManagedOrgUnits(employment.getMunicipality().getId(), employmentId);
			result.addAll(getManagers(managedOrgUnits));
		} else {
			log.warn("trying to get subordinates but found no employment for: {}", employmentId);
		}
		return result;
	}

	private List<EmploymentPO> getManagers(List<OrgUnit> orgUnits){
		List<EmploymentPO> result = new ArrayList<>();
		List<Employment> managers = new ArrayList<>();
		getManagersAcc(orgUnits, managers);
		for (Employment manager : managers) {
			result.add(new EmploymentPO(manager));
		}
		return result;
	}

	private void getManagersAcc(List<OrgUnit> orgUnits, List<Employment> managersAcc){
		if(orgUnits != null && !orgUnits.isEmpty()){
			for (OrgUnit orgUnit : orgUnits) {
				if(orgUnit.getManager().isPresent()){
					managersAcc.add(orgUnit.getManager().get());
				}
				getManagersAcc(orgUnit.getChildren(), managersAcc);
			}
		}
	}

	private Municipality getMunicipality(long id){
		return persistence.getEm().find(Municipality.class, id);
	}

	private long getSearchCount(EmploymentSearch search, Municipality municipality){
		CriteriaBuilder builder = getEm().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root root = criteriaQuery.from(Employment.class);
		criteriaQuery.select(builder.count(root));

		List<Predicate> predicates = new ArrayList<Predicate>();
		predicates.add(builder.equal(root.get(Employment_.municipality), municipality));
		if(search.getNameTerm() != null && !search.getNameTerm().isEmpty()){
			String nameTerm = "%"+search.getNameTerm().toUpperCase()+"%";
			predicates.add(builder.like(builder.upper(root.get(Employment_.name)), nameTerm));
		}
		if(search.getInitialsTerm() != null && !search.getInitialsTerm().isEmpty()){
			String initialsTerm = "%"+search.getInitialsTerm().toUpperCase()+"%";
			predicates.add(builder.like(builder.upper(root.get(Employment_.initials)), initialsTerm));
		}
		criteriaQuery.where(predicates.toArray(new Predicate[]{}));
		Query countQuery = getEm().createQuery(criteriaQuery);

		return (long) countQuery.getSingleResult();
	}

	private List<EmploymentPO> getSearchResults(EmploymentSearch search, Municipality municipality){
		CriteriaBuilder builder = getEm().getCriteriaBuilder();
		CriteriaQuery<Employment> criteriaQuery = builder.createQuery(Employment.class);
		Root<Employment> root = criteriaQuery.from(Employment.class);

		List<Predicate> predicates = new ArrayList<Predicate>();
		predicates.add(builder.equal(root.get(Employment_.municipality), municipality));
		if(search.getNameTerm() != null && !search.getNameTerm().isEmpty()){
			String nameTerm = "%"+search.getNameTerm().toUpperCase()+"%";
			predicates.add(builder.like(builder.upper(root.get(Employment_.name)), nameTerm));
		}
		if(search.getInitialsTerm() != null && !search.getInitialsTerm().isEmpty()){
			String initialsTerm = "%"+search.getInitialsTerm().toUpperCase()+"%";
			predicates.add(builder.like(builder.upper(root.get(Employment_.initials)), initialsTerm));
		}
		criteriaQuery.orderBy(builder.asc(root.get(Employment_.name)));
		criteriaQuery.where(predicates.toArray(new Predicate[]{}));
		Query resultsQuery = getEm().createQuery(criteriaQuery);

		// results query
		resultsQuery.setFirstResult(search.getOffset());
		resultsQuery.setMaxResults(search.getPageSize());

		List<Employment> employments = resultsQuery.getResultList();
		return employments.stream().map(EmploymentPO::new).collect(Collectors.toList());
	}

	private EntityManager getEm(){
		if(em == null){
			em = persistence.getEm();
		}
		return em;
	}

}
