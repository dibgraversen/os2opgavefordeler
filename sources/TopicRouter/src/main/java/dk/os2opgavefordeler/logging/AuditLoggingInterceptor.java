package dk.os2opgavefordeler.logging;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.*;

import dk.os2opgavefordeler.auth.AuthService;
import dk.os2opgavefordeler.model.LogEntry;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.service.AuditLogService;
import dk.os2opgavefordeler.service.MunicipalityService;
import dk.os2opgavefordeler.service.UserService;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rro on 02-02-2017.
 */
@AuditLogged
@Interceptor
public class AuditLoggingInterceptor {

	@Inject
	private AuditLogService auditLogService;

	@Inject
	private Logger logger;

	@Inject
	private AuthService authService;

	@Inject
	private UserService userService;

	@Inject
	MunicipalityService municipalityService;

	/**
	 * Intercepts a method call, and generates audit logging.
	 * @param ic
	 * @return
	 * @throws Exception
	 */
	@AroundInvoke
	private Object intercept(InvocationContext ic) throws Exception{

		User user = userService.findByEmail(authService.getAuthentication().getEmail()).get();

		Method method = ic.getMethod();
		String methodName = extractMethodName(method.toGenericString());

		String operationTypeString = "Unable to identify operation type";
		Annotation methodRestAnnotation = getRestAnnotation(method);
		if(methodRestAnnotation != null){
			operationTypeString = extractMethodName(methodRestAnnotation.toString());
		}

		String parameters = buildParameterString(ic);

		LogEntry logEntry = new LogEntry("", user.getEmail(), operationTypeString, methodName, parameters, "", "", user.getMunicipality());
		auditLogService.saveLogEntry(logEntry);

		return ic.proceed();
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

	/**
	 * Extracts the actual name from either endpoint method full path or REST method annotation full path.
	 * @param input string to extract from
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
}
