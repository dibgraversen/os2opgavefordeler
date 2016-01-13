package dk.os2opgavefordeler.auth;


import org.apache.deltaspike.security.api.authorization.SecurityBindingType;

import javax.inject.Qualifier;
import java.lang.annotation.*;

@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Documented
@Qualifier
public @interface CurrentUser {
}
