package dk.os2opgavefordeler.service;

public class AuthorizationException extends Exception {
	private String message, userMessage;

	public AuthorizationException(String message) {
		this(message, null);
	}

	public AuthorizationException(String message, String userMessage) {
		this.message = message;
		this.userMessage = userMessage;
	}
}
