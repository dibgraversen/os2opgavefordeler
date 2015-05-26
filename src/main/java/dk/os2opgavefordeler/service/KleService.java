package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Kle;

import java.util.List;
import java.util.Optional;

public interface KleService {
	List<Kle> fetchAllKleMainGroups();
	Optional<Kle> fetchMainGroup(String number);

	void storeAllKleMainGroups(List<Kle> groups);
}
