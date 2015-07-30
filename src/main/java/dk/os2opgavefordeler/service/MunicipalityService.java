package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Municipality;

import java.util.List;

/**
 * @author hlo@miracle.dk
 */
public interface MunicipalityService {
	Municipality createMunicipality(Municipality municipality);
	Municipality getMunicipality(long id);
	List<Municipality> getMunicipalities();

	Municipality findByName(String name);
}
