package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.presentation.EmploymentPO;

import java.util.List;
import java.util.Optional;

public interface EmploymentService {
	Optional<Employment> getEmployment(int id);
	Optional<EmploymentPO> getEmploymentPO(int id);
	List<Employment> findByEmail(String email);

	List<Employment> getAll(int municipalityId);
	List<EmploymentPO> getAllPO(int municipalityId);
}
