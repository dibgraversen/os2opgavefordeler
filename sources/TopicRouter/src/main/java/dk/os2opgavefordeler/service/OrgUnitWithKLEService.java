package dk.os2opgavefordeler.service;

import java.util.List;

import dk.os2opgavefordeler.model.presentation.KleAssignmentType;
import dk.os2opgavefordeler.model.presentation.OrgUnitWithKLEPO;

public interface OrgUnitWithKLEService {
	
	List<OrgUnitWithKLEPO> getAll(long municipalityId); 
	OrgUnitWithKLEPO get(long id);
	boolean addKLE(long ouId, String kleNumber, KleAssignmentType assignmentType);
	boolean removeKLE(long ouId, String kleNumber, KleAssignmentType assignmentType);

}
