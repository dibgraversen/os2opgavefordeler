package dk.os2opgavefordeler.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Super class for endpoints providing general methods for returning responses etc.
 *
 * @author hlo@miracle.dk
 */
public abstract class Endpoint {

	static final int PAYMENT_REQUIRED = 402;

	static final String TEXT_PLAIN = "text/plain";

	private static final Status BAD_REQUEST = Status.BAD_REQUEST;

	/**
	 * Creates an error response.
	 *
	 * @param reason The plain text error message wanted as 'entity'.
	 * @return a built Response with status 'bad request', type 'text/plain' and 'reason' as entity.
	 */
	protected Response badRequest(String reason) {
		return Response.status(BAD_REQUEST).type(TEXT_PLAIN).entity(reason).build();
	}

	/**
	 * Creates an ok response.
	 *
	 * @return a built Response with status ok and no entity set.
	 */
	public Response ok() {
		return Response.ok().build();
	}

	/**
	 * Builds an ok response with object
	 *
	 * @param result The Object wanted as 'entity'
	 * @return a built Response with status ok and 'result' as entity.
	 */
	public Response ok(Object result) {
		return Response.ok().entity(result).build();
	}

	/**
	 * Creates a forbidden response.
	 *
	 * @return a built Response with status forbidden and no entity set.
	 */
	public Response forbidden() {
		return Response.status(Response.Status.FORBIDDEN).build();
	}

	/**
	 * Creates a not found response.
	 *
	 * @return a built Response with not found and no entity set.
	 */
	public Response notFound() {
		return Response.status(Status.NOT_FOUND).build();
	}
}
