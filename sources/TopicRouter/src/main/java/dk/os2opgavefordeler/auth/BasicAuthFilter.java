package dk.os2opgavefordeler.auth;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.StringTokenizer;

@WebFilter
public class BasicAuthFilter implements Filter {

    @Inject
    private Logger logger;

    @Inject
    private AuthService authService;

	private static final String AUTH_BASIC = "Basic";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // not currently implemented
    }

    private void authenticate(HttpServletRequest request) throws Exception {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null) {
            return;
        }

        StringTokenizer st = new StringTokenizer(authHeader);

        if (!st.hasMoreTokens()) {
            logger.info("Malformed auth header, no tokens.");
            return;
        }

        String authType = st.nextToken();

	    if (!AUTH_BASIC.equalsIgnoreCase(authType)) {
		    logger.info("Auth is not basic.");
		    return;
	    }

        String credentials = new String(Base64.decodeBase64(st.nextToken()), "UTF-8");

        int p = credentials.indexOf(':');

        if (p == -1) {
            logger.info("Auth is malformed, no ':' given.");
            return;
        }

        String email = credentials.substring(0, p).trim();
        String municipalityToken = credentials.substring(p + 1).trim();

        authService.authenticateWithEmailAndToken(email, municipalityToken);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        try {
            authenticate(request);
        }
        catch (Exception e) {
	        logger.error("Error while authenticating: ", e);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
	    // not currently implemented
    }
}
