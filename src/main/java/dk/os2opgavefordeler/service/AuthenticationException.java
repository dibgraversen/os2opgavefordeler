package dk.os2opgavefordeler.service;

public class AuthenticationException extends Exception {
	private Throwable inner;
	private String message, userMessage;

	public AuthenticationException(String message) {
		this(message, null, null);
	}

	public AuthenticationException(String message, Throwable inner) {
		this(message, null, inner);
	}

	public AuthenticationException(String message, String userMessage, Throwable inner) {
		this.message = message;
		this.userMessage = userMessage;
		this.inner = inner;
	}
}
