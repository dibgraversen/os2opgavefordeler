package dk.os2opgavefordeler.auth.openid;

import com.google.common.collect.Lists;
import dk.os2opgavefordeler.employment.EmploymentRepository;
import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Role;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.service.UserService;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import java.util.Optional;

@ApplicationScoped
public class OpenIdUserFactory {

    @Inject
    private Logger log;

    @Inject
    private UserService userService;

    @Inject
    private EmploymentRepository employmentRepository;


    public User createUserFromOpenIdEmail(String email) {
        // In order to create a User from an OpenID Connect login, we require the email to be present in a municipality.
        //
        // An email address can be used for several Employments. For instance, it's possible for a manager to also have
        // non-manager employment - so we create a role of each of the employment.
        //
        Employment employment;
        try {
            employment = employmentRepository.findByEmail(email);
        } catch (NoResultException e) {
            throw new RuntimeException("No employments found for " + email);
        }

        //TODO: the following code has somewhat of a smell to it. Probably 99% of the time, there will be a single
        //employment associated with an email address - but it feels a bit icky simply grabbing the first employment
        //for the other cases.
        final Role role = createRolesFromEmployments(employment);

        final String name = employment.getName();
        final User user = new User(name, email, Lists.newArrayList(role));

        employment.getEmployedIn().getMunicipality().ifPresent(user::setMunicipality);

        log.info("Persising {} with roles={}", user, role);
        return userService.createUser(user);
    }

    private Role createRolesFromEmployments(Employment emp) {

        final Optional<Employment> departmentManager = emp.getEmployedIn().getManager();
        final Role role = new Role();

        role.setManager(departmentManager.map(m -> m.equals(emp)).orElse(false));
        role.setEmployment(emp);
        role.setName(String.format("%s (%s)", emp.getName(), emp.getEmployedIn().getName()));

        return role;

    }

}
