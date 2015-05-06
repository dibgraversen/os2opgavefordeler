package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.kle.KleMainGroup;

import java.util.List;

public interface PersistenceService {
	List<KleMainGroup> fetchAllKleMainGroups();
	KleMainGroup fetchMainGroup(String number);

	void storeAllKleMainGroups(List<KleMainGroup> groups);

}
