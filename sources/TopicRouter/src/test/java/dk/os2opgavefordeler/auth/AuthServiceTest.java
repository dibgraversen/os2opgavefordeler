package dk.os2opgavefordeler.auth;

import dk.os2opgavefordeler.repository.UserRepository;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.test.UnitTest;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.ArrayList;

import static org.junit.Assert.*;

@Category(UnitTest.class)
@RunWith(CdiTestRunner.class)
@TestControl(projectStage = ProjectStage.Development.class)
public class AuthServiceTest {

    @Inject
    private AuthService authService;

    @Inject
    private UserRepository userRepository;

    @Before
    public void setup() {
        Municipality m = new Municipality("foo");
        User user = new User("foo", "foo@bar.com", new ArrayList<>());
        user.setMunicipality(m);
        userRepository.saveAndFlushAndRefresh(user);
    }

    @Test
    public void testIsAuthenticated() throws Exception {
        assertFalse(authService.isAuthenticated());
        authService.authenticateAs("foo@bar.com");
        assertTrue(authService.isAuthenticated());
    }

    @Test
    public void testGetAuthentication() throws Exception {
        Authentication a = new Authentication("foo@bar.com");
        authService.authenticateAs("foo@bar.com");
        assertEquals(a, authService.getAuthentication());
    }

    @Test
    public void testLogout() throws Exception {
        authService.authenticateAs("foo@bar.com");
        assertTrue(authService.isAuthenticated());
        authService.logout();
        assertFalse(authService.isAuthenticated());
    }

}