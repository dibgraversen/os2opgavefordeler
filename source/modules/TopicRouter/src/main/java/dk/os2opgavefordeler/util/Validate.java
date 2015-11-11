package dk.os2opgavefordeler.util;

import dk.os2opgavefordeler.service.BadRequestArgumentException;

public class Validate {
	public static void nonZero(Long value, String message) throws BadRequestArgumentException
	{
		if(value == null || value == 0) {
			throw new BadRequestArgumentException(message);
		}
	}
}
