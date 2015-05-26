package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Kle;

import java.util.List;

public interface KleService {
	List<Kle> fetchAllKleMainGroups();
	Kle fetchMainGroup(String number);

	void storeAllKleMainGroups(List<Kle> groups);
}
