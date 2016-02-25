package dk.os2opgavefordeler.service;


public interface ConfigService {
	String getHomeUrl();
	String getOpenIdCallbackUrl();
	boolean isGodModeLoginEnabled();
	boolean enableGoogleLogin();
}
