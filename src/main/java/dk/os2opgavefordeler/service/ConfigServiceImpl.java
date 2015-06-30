package dk.os2opgavefordeler.service;

public class ConfigServiceImpl implements ConfigService {
	public static final String callback_url = "http://localhost:8080/TopicRouter/rest/auth/authenticate";
	public static final String home_url = "http://localhost:9001/";	//TODO: property? Or should we pick it up from the original request referer?

	@Override
	public String getHomeUrl() {
		return home_url;
	}

	@Override
	public String getCallbackUrl() {
		return callback_url;
	}
}
