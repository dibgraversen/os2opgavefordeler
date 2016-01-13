package dk.os2opgavefordeler.auth;

import org.apache.commons.codec.binary.Base64;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

@WebFilter
public class BasicAuthFilter implements Filter {
    public static final String ATTRIBUTE_USER_EMAIL = "BasicAuthFilter_USER_EMAIL";
    public static final String ATTRIBUTE_USER_TOKEN = "BasicAuthFilter_USER_TOKEN";
    private static final Logger logger = Logger.getLogger(BasicAuthFilter.class.getName());

    @Inject
    private LoginController controller;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    private void authenticate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null) {
            logger.info("No auth header");
            return;
        }

        StringTokenizer st = new StringTokenizer(authHeader);
        if (!st.hasMoreTokens()) {
            logger.info("Malformed auth header, no tokens.");
            return;
        }
        String basic = st.nextToken();
        if (!basic.equalsIgnoreCase("Basic")) {
            logger.info("Auth is not basic.");
            return;
        }

        String credentials = new String(Base64.decodeBase64(st.nextToken()), "UTF-8");
        logger.info("Credentials: " + credentials);
        int p = credentials.indexOf(":");

        if (p == -1) {
            logger.info("Auth is malformed, no : given.");
            return;
        }

        String email = credentials.substring(0, p).trim();
        String municipality_token = credentials.substring(p + 1).trim();

        controller.login(email, municipality_token);

        request.setAttribute(ATTRIBUTE_USER_EMAIL, email);
        request.setAttribute(ATTRIBUTE_USER_TOKEN, municipality_token);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            authenticate(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
