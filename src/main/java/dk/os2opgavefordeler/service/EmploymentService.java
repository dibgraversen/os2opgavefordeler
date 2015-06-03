package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Employment;
import java.util.Optional;

public interface EmploymentService {
	Optional<Employment> getEmployment(int id);
}
