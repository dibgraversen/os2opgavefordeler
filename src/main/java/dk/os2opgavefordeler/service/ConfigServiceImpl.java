package dk.os2opgavefordeler.service;

import org.slf4j.Logger;

import javax.inject.Inject;

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


	private String getProperty(String property, String defaultValue) {
		final String value = System.getProperty(property);

		if(value == null) {
			log.warn("Property {} is not set, return default", property);
			return defaultValue;
		}

		return value;
	}
}
