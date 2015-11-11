package dk.os2opgavefordeler.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * @author hlo@miracle.dk
 */
public abstract class Endpoint {
	public static final int PAYMENT_REQUIRED = 402;
	public static final String TEXT_PLAIN = "text/plain";
	public static final Status BAD_REQUEST = Status.BAD_REQUEST;

	/**
	 * Creates an error response.
	 * @param reason The plain text error message wanted as 'entity'.
	 * @return A built Response with status 'bad request', type 'text/plain' and 'reason' as
	 * entity.
	 */
	public Response badRequest(String reason){
		return Response.status(BAD_REQUEST).type(TEXT_PLAIN).entity(reason).build();
	}

	/**
	 * Creates an ok response.
	 * @return a Built Response with status ok and no entity set.
	 */
	public Response ok(){
		return Response.ok().build();
	}

	/**
	 * Builds an ok response with object
	 * @param result The Object wanted as 'entity'
	 * @return A built Response with status ok and 'result' as entity.
	 */
	public Response ok(Object result){
		return Response.ok().entity(result).build();
	}
}
