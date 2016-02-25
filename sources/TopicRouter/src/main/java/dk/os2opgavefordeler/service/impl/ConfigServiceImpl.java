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

	@Override
	public String getHomeUrl() {
		return getProperty("topicrouter.url.home", "http://localhost:9001/");
	}

	@Override
	public String getOpenIdCallbackUrl() {
		return getProperty("topicrouter.url.openid.callback", "http://localhost:8080/TopicRouter/rest/auth/authenticate");
	}

	@Override
	public boolean isGodModeLoginEnabled() {
		return getProperty("topicrouter.login.godmode.enabled", false);
	}

	@Override
	public boolean enableGoogleLogin() {
		return getProperty("topicrouter.login.google.enabled", false);
	}

	private String getProperty(String property, String defaultValue) {
		return getProperty(property, defaultValue, s -> s);
	}

	private boolean getProperty(String property, boolean defaultValue) {
		return getProperty(property, defaultValue, s -> Boolean.valueOf(s));
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
