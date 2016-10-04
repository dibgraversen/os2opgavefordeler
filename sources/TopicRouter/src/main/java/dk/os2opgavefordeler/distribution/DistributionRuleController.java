package dk.os2opgavefordeler.distribution;

import dk.os2opgavefordeler.auth.AuthService;
import dk.os2opgavefordeler.distribution.dto.CprDistributionRuleFilterDTO;
import dk.os2opgavefordeler.distribution.dto.TextDistributionRuleFilterDTO;
import dk.os2opgavefordeler.logging.AuditLogger;
import dk.os2opgavefordeler.service.UserService;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

import javax.enterprise.context.ApplicationScoped;

import javax.inject.Inject;

import javax.persistence.EntityManager;

import dk.os2opgavefordeler.distribution.dto.DistributionRuleFilterDTO;

import dk.os2opgavefordeler.employment.EmploymentRepository;
import dk.os2opgavefordeler.employment.OrgUnitRepository;

import dk.os2opgavefordeler.model.*;

import java.util.Optional;

@ApplicationScoped
@Transactional
public class DistributionRuleController {

    @Inject
    private DistributionRuleRepository ruleRepository;

    @Inject
    private OrgUnitRepository orgUnitRepository;

    @Inject
    private EmploymentRepository employmentRepository;

    @Inject
    private EntityManager entityManager;

    @Inject
    private DistributionRuleFilterFactory filterFactory;

    @Inject
    private AuditLogger auditLogger;

    @Inject
    UserService userService;

    @Inject
    private AuthService authService;

	/**
	 * Creates a new distribution rule filter
	 *
	 * @param dto DTO object for the distribution rule filter
	 * @throws OrgUnitNotFoundException
	 * @throws RuleNotFoundException
	 */
    public void createFilter(DistributionRuleFilterDTO dto) throws
            OrgUnitNotFoundException,
            RuleNotFoundException {

        DistributionRule rule = ruleRepository.findBy(dto.distributionRuleId);

        if (rule == null) {
            throw new RuleNotFoundException("No such rule" + dto.distributionRuleId);
        }

        OrgUnit orgUnit = orgUnitRepository.findBy(dto.assignedOrgId);

        if (orgUnit == null) {
            throw new OrgUnitNotFoundException("Organizational unit not found");
        }

        rule.addFilter(filterFactory.fromDto(dto));

        ruleRepository.save(rule);

        logEvent(rule, dto, orgUnit, LogEntry.CREATE_TYPE); // log event
    }

	/**
	 * Updates the distribution rule filter with the specified filter ID
	 *
	 * @param ruleId ID for the distribution rule
	 * @param filterId ID for the distribution rule filter
	 * @param dto DTO object to use when updating the distribution rule filter
	 * @throws OrgUnitNotFoundException
	 * @throws RuleNotFoundException
	 */
    public void updateFilter(long ruleId, long filterId, DistributionRuleFilterDTO dto) throws
            OrgUnitNotFoundException,
            RuleNotFoundException {
        DistributionRule rule = ruleRepository.findBy(ruleId);

        if (rule == null) {
            throw new RuleNotFoundException("No such rule" + dto.distributionRuleId);
        }

        OrgUnit orgUnit = orgUnitRepository.findBy(dto.assignedOrgId);

        if (orgUnit == null) {
            throw new OrgUnitNotFoundException("Organizational unit not found");
        }

        DistributionRuleFilter filterById = rule.getFilterById(filterId);
        filterById.setName(dto.name);

        Employment e = employmentRepository.findBy(dto.assignedEmployeeId);

	    if (e != null) {
            filterById.setAssignedEmployee(e);
        }

        OrgUnit o = orgUnitRepository.findBy(dto.assignedOrgId);

	    if (o != null) {
            filterById.setAssignedOrg(o);
        }

        if (filterById instanceof CprDistributionRuleFilter) {
            CprDistributionRuleFilter f = (CprDistributionRuleFilter) filterById;
            f.setDays(dto.days);
            f.setMonths(dto.months);
        }
        else if (filterById instanceof TextDistributionRuleFilter) {
            TextDistributionRuleFilter f = (TextDistributionRuleFilter) filterById;
            f.setText(dto.text);
        }

        entityManager.merge(filterById);

        logEvent(rule, dto, orgUnit, LogEntry.UPDATE_TYPE); // log event
    }

	/**
	 * Deletes the distribution rule filter with the specified distribution rule ID and filter ID
	 *
	 * @param distributionRuleId ID for the distribution rule
	 * @param filterId ID for the distribution rule filter
	 */
    public void deleteFilter(long distributionRuleId, long filterId) {
        DistributionRule rule = ruleRepository.findBy(distributionRuleId);

        DistributionRuleFilter filterById = rule.getFilterById(filterId);

        OrgUnit orgUnit = filterById.getAssignedOrg();
        Employment employment = filterById.getAssignedEmployee();

        String dataStr = "";

        if (filterById instanceof CprDistributionRuleFilter) {
            CprDistributionRuleFilter f = (CprDistributionRuleFilter) filterById;
            dataStr = "Navn: " + f.getName() + "; Dage: " + f.getDays() + "; Måneder: " + f.getMonths();
        }
        else if (filterById instanceof TextDistributionRuleFilter) {
            TextDistributionRuleFilter f = (TextDistributionRuleFilter) filterById;
            dataStr = "Navn: " + f.getName() + "; Tekst: " + f.getText();
        }

        final Optional<User> user = userService.findByEmail(authService.getAuthentication().getEmail());
        final String userStr = user.isPresent() ? user.get().getEmail() : "";
        final String orgUnitStr = orgUnit != null ? orgUnit.getName() + " (" + orgUnit.getBusinessKey() + ")" : "";
        final String employmentStr = employment != null ? employment.getName() + " (" + employment.getInitials() + ")" : "";
        final Municipality municipality = user.isPresent() ? user.get().getMunicipality() : null;

        // log event
        auditLogger.event(rule.getKle().getNumber(), userStr, LogEntry.DELETE_TYPE, LogEntry.EXTENDED_DISTRIBUTION_TYPE, dataStr, orgUnitStr, employmentStr, municipality);


        filterById.setDistributionRule(null);
        rule.removeFilter(filterById);

        entityManager.remove(filterById);
        ruleRepository.save(rule);
    }

    public class RuleNotFoundException extends Exception {
        public RuleNotFoundException(String msg) {
            super(msg);
        }

        public RuleNotFoundException(String msg, Throwable t) {
            super(msg, t);
        }
    }

    public class OrgUnitNotFoundException extends Exception {
        public OrgUnitNotFoundException(String msg) {
            super(msg);
        }

        public OrgUnitNotFoundException(String msg, Throwable t) {
            super(msg, t);
        }
    }

    private void logEvent(DistributionRule rule, DistributionRuleFilterDTO dto, OrgUnit orgUnit, String logType) {
        final Optional<User> user = userService.findByEmail(authService.getAuthentication().getEmail());
        final Employment employment = employmentRepository.findBy(dto.assignedEmployeeId);

        final String userStr = user.isPresent() ? user.get().getEmail() : "";
        final Municipality municipality = user.isPresent() ? user.get().getMunicipality() : null;
        final String orgUnitStr = orgUnit.getName() + " (" + orgUnit.getBusinessKey() + ")";
        final String employmentStr = employment != null ? employment.getName() + " (" + employment.getInitials() + ")" : "";

        String dataStr = "";

        if (CprDistributionRuleFilterDTO.FILTER_TYPE.equals(dto.type)) {
            dataStr = "Navn: " + dto.name + "; Dage: " + dto.days + "; Måneder: " + dto.months;
        }
        else if (TextDistributionRuleFilterDTO.FILTER_TYPE.equals(dto.type)) {
            dataStr = "Navn: " + dto.name + "; Tekst: " + dto.text;
        }

        // log event
        auditLogger.event(rule.getKle().getNumber(), userStr, logType, LogEntry.EXTENDED_DISTRIBUTION_TYPE, dataStr, orgUnitStr, employmentStr, municipality);
    }
}
