package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.presentation.EmploymentPO;

import java.util.List;
import java.util.Optional;

public interface EmploymentService {
	Optional<Employment> getEmployment(long id);
	Optional<EmploymentPO> getEmploymentPO(long id);
	List<Employment> findByEmail(String email);

	List<Employment> getAll();
	List<EmploymentPO> getAllPO();
}
