package dk.os2opgavefordeler.service;

public class BadRequestArgumentException extends Exception {
	public BadRequestArgumentException() {
		super();
	}

	public BadRequestArgumentException(String message) {
		super(message);
	}

	public BadRequestArgumentException(String message, Throwable cause) {
		super(message, cause);
	}
}
