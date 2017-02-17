package dk.os2opgavefordeler.service;

import java.util.List;
import java.util.Optional;

import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.presentation.KleAssignmentType;
import dk.os2opgavefordeler.model.presentation.OrgUnitListPO;
import dk.os2opgavefordeler.model.presentation.OrgUnitWithKLEPO;

public interface OrgUnitWithKLEService {
	
	List<OrgUnitWithKLEPO> getAll(long municipalityId); 
	OrgUnitWithKLEPO get(long id, Municipality municipality);
	boolean addKLE(long ouId, String kleNumber, KleAssignmentType assignmentType);
	boolean removeKLE(long ouId, String kleNumber, KleAssignmentType assignmentType);
	boolean containsKLE(OrgUnit ou, KleAssignmentType assignmentType, String kleNumber);
	List<OrgUnitListPO> getList(long municipalityId);

}
