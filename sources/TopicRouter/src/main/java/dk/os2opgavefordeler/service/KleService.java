package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Kle;

import java.util.List;
import java.util.Optional;

public interface KleService {
	List<Kle> fetchAllKleMainGroups();

	Optional<Kle> fetchMainGroup(String number, long municipalityId);

	void storeAllKleMainGroups(List<Kle> groups);

	Kle getKle(Long id);

	Kle getKle(String kleNumber);
}
