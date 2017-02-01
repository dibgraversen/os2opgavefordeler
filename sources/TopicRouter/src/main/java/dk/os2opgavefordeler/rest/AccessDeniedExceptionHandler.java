package dk.os2opgavefordeler.rest;

import org.apache.deltaspike.security.api.authorization.AccessDeniedException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author hlo@miracle.dk
 */
@Provider
public class AccessDeniedExceptionHandler implements ExceptionMapper<AccessDeniedException> {

	@Override
	public Response toResponse(AccessDeniedException e) {
		return Response.status(Response.Status.FORBIDDEN).entity("User not logged in.").build();
	}
}
