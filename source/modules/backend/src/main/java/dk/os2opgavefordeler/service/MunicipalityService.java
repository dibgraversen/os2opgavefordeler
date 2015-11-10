package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.ValidationException;
import dk.os2opgavefordeler.model.presentation.KlePO;

import java.util.List;
import java.util.Optional;

/**
 * @author hlo@miracle.dk
 */
public interface MunicipalityService {
	Municipality createMunicipality(Municipality municipality);

	Municipality createOrUpdateMunicipality(Municipality municipality);

	Municipality getMunicipality(long id);
	Optional<Municipality> getMunicipalityFromToken(String token);
	List<Municipality> getMunicipalities();

	Municipality findByName(String name);

	KlePO saveMunicipalityKle(KlePO kle) throws ValidationException;
	void deleteMunicipalityKle(long municipalityId, long kleId) throws ValidationException;
	List<KlePO> getMunicipalityKle(long municipalityId);
}
