package dk.os2opgavefordeler.service;


public interface ConfigService {
	String getHomeUrl();
	String getOpenIdCallbackUrl();
	String getClientId();
	String getClientSecret();
	boolean isGodModeLoginEnabled();
	boolean isAuditLogEnabled();
	boolean isExtendedResponsibilityEnabled();
	boolean isAuditTraceEnabled();
}
