package dk.os2opgavefordeler.auth;

import org.apache.deltaspike.security.api.authorization.SecurityBindingType;

import java.lang.annotation.*;

/**
 * Created by rro rro@miracle.dk on 02-02-2017.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@SecurityBindingType
public @interface MunicipalityAdminRequired {}