package dk.os2opgavefordeler.auth;

import org.apache.deltaspike.security.api.authorization.SecurityBindingType;

import java.lang.annotation.*;

@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@SecurityBindingType
public @interface LoggedInUser {}