package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.Kle;
import dk.os2opgavefordeler.model.kle_import.KLEEmneplanKomponent;

import java.util.List;

public interface KleImportMapper {
	List<Kle> mapMainGroupList(KLEEmneplanKomponent input);
}
