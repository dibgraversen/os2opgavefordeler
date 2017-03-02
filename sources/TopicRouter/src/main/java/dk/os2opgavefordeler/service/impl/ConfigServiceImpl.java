package dk.os2opgavefordeler.service.impl;

import dk.os2opgavefordeler.service.ConfigService;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.function.Function;

@ApplicationScoped
public class ConfigServiceImpl implements ConfigService {
	@Inject
	Logger log;

	private static final String URL_HOME_PROPERTY_NAME = "topicrouter.url.home";
	private static final String OPEN_ID_CALLBACK_PROPERTY_NAME = "topicrouter.url.openid.callback";
	private static final String GODMODE_ENABLED_PROPERTY_NAME = "topicrouter.login.godmode.enabled";
	private static final String EXTENDED_RESPONSIBILITY_ENABLED_PROPERTY_NAME = "topicrouter.extendedresponsibility.enabled";
	private static final String GOOGLE_LOGIN_ENABLED_PROPERTY_NAME = "topicrouter.login.google.enabled";
	private static final String ENABLE_AUDIT_LOGGING_PROPERTY_NAME = "topicrouter.auditlog.enabled";
	private static final String AUDIT_TRACE_ENABLED_NAME = "topicrouter.auditlog.trace.enabled";

	@Override
	public String getHomeUrl() {
		return getProperty(URL_HOME_PROPERTY_NAME, "http://localhost:9001/");
	}

	@Override
	public String getOpenIdCallbackUrl() {
		return getProperty(OPEN_ID_CALLBACK_PROPERTY_NAME, "http://localhost:8080/TopicRouter/rest/auth/authenticate");
	}

	@Override
	public boolean isGodModeLoginEnabled() {
		return getProperty(GODMODE_ENABLED_PROPERTY_NAME, false);
	}

	@Override
	public boolean isExtendedResponsibilityEnabled() {
		return getProperty(EXTENDED_RESPONSIBILITY_ENABLED_PROPERTY_NAME, false);
	}

	@Override
	public boolean enableGoogleLogin() {
		return getProperty(GOOGLE_LOGIN_ENABLED_PROPERTY_NAME, false);
	}

	@Override
	public boolean isAuditLogEnabled() {
		return getProperty(ENABLE_AUDIT_LOGGING_PROPERTY_NAME, true);
	}

	@Override
	public boolean isAuditTraceEnabled() {
		return getProperty(AUDIT_TRACE_ENABLED_NAME, false);
	}

	private String getProperty(String property, String defaultValue) {
		return getProperty(property, defaultValue, s -> s);
	}

	private boolean getProperty(String property, boolean defaultValue) {
		return getProperty(property, defaultValue, Boolean::valueOf);
	}

	private <T> T getProperty(String property, T defaultValue, Function<String, T> converter) {
		final String value = System.getProperty(property);

		if(value == null) {
			log.warn("Property {} is not set, return default", property);
			return defaultValue;
		}

		return converter.apply(value);
	}
}
