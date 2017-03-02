package dk.os2opgavefordeler.logging;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.*;

import dk.os2opgavefordeler.LoggedInUser;
import dk.os2opgavefordeler.model.LogEntry;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.service.AuditLogService;
import dk.os2opgavefordeler.service.ConfigService;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author rro@miracle.dk
 */
@AuditLogged
@Interceptor
public class AuditLoggingInterceptor {

	@Inject
	private AuditLogService auditLogService;

	@Inject
	private Logger logger;

	@Inject @LoggedInUser
	User currentUser;

	@Inject
	ConfigService configService;

	@AroundInvoke
	private Object intercept(InvocationContext ic) throws Exception {
		LogEntry logEntry = new LogEntry();
		Method method = ic.getMethod();
		Operation operation = getOperation(method);
		if( notTraceLogging(operation) || traceLoggingEnabled() ){
			populateLogEntry(logEntry, ic, method, operation);
			auditLogService.saveLogEntry(logEntry);
		}
		return ic.proceed();
	}

	private void populateLogEntry(LogEntry logEntry, InvocationContext ic, Method method, Operation operation){
		logEntry.setType(extractMethodName(method.toGenericString()));
		logEntry.setData(buildParameterString(ic));
		logEntry.setOperation(operation.name());
		if(currentUser != null){
			logEntry.setUser(currentUser.getEmail());
			logEntry.setMunicipality(currentUser.getMunicipality());
		}
	}

	/**
	 * Builds a comma-space separated string of method parameter annotation and corresponding value pairs, eg.: "userId=2, municipalityId=3".
	 * @param ic invocation context
	 * @return comma-space separated string of annotation and value pairs.
	 */
	private String buildParameterString(InvocationContext ic){
		String parameterString = "";
		Annotation[][] annotations = ic.getMethod().getParameterAnnotations(); // Each index in first dimension represents a method parameter. Each index in second dimension is an annotation for the parameter.
		Object[] parameterValues = ic.getParameters(); // Array of parameter values. Indexes correspond to first dimension of annotations array.
		if(annotations.length > 0){
			for(int i = 0; i < annotations.length; i++){
				if(i > 0){
					parameterString += ", ";
				}
				if(annotations[i].length > 0){
					Annotation currentAnnotation = annotations[i][0];
					if(currentAnnotation instanceof PathParam){
						parameterString += ((PathParam) currentAnnotation).value() + "=" + parameterValues[i];
					}
					else if(currentAnnotation instanceof QueryParam){
						parameterString += ((QueryParam) currentAnnotation).value() + "=" + parameterValues[i];
					}
				}
			}
		}
		else{
			parameterString = "No parameters";
		}
		return parameterString;
	}

	/**
	 * Searches for POST, GET, PUT or DELETE annotations on the supplied method, and returns the first found. If none of those rest annotations are found, null is returned.
	 * @param method to search.
	 * @return First POST, GET or DELETE annotation found. Null if none found.
	 */
	private Annotation getRestAnnotation(Method method){
		Annotation result = null;
		Annotation[] annotations = method.getAnnotations();
		for (Annotation currentAnnotation : annotations) {
			if(currentAnnotation instanceof POST || currentAnnotation instanceof GET || currentAnnotation instanceof PUT ||currentAnnotation instanceof DELETE){
				result = currentAnnotation;
			}
		}
		return result;
	}

	private Operation getOperation(Method method){
		Operation result = Operation.GET;
		Annotation methodRestAnnotation = getRestAnnotation(method);
		if(methodRestAnnotation != null){
			result = Operation.fromString(extractMethodName(methodRestAnnotation.toString()));
			if(result == null){
				logger.warn("did not find operation for: {}", methodRestAnnotation.toString());
			}
		}
		return result;
	}

	/**
	 * Extracts the actual name from either endpoint method full path or REST method annotation full path.
	 * @return the actual name.
	 */
	private String extractMethodName(String input){

		String result = "";
		String restStringPattern = "(.*)\\.(.*)\\(.*\\)";

		Pattern pattern;
		pattern = Pattern.compile(restStringPattern);

		Matcher matcher = pattern.matcher(input);
		if(matcher.find()){
			result = matcher.group(2);
		}

		return result;
	}

	private boolean notTraceLogging(Operation operation){
		return operation != Operation.GET;
	}

	private boolean traceLoggingEnabled(){
		return configService.isAuditTraceEnabled();
	}

	private enum Operation {
		GET("GET"),
		PUT("PUT"),
		POST("POST"),
		DELETE("DELETE");
		private String name;

		Operation(String name){
			this.name = name;
		}

		public static Operation fromString(String operationName) {
			for (Operation op : Operation.values()) {
				if (op.name.equalsIgnoreCase(operationName)) {
					return op;
				}
			}
			return null;
		}
	}
}
