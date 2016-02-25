package dk.os2opgavefordeler.auth.provider;

import dk.os2opgavefordeler.model.IdentityProvider;
import dk.os2opgavefordeler.model.presentation.IdentityProviderPO;
import dk.os2opgavefordeler.service.ConfigService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class ProviderRepository {

    private final Map<Long, IdentityProvider> providers = new HashMap<>();
    
    @Inject
    private ConfigService configService;

    @PostConstruct
    public void init() {

        if (configService.enableGoogleLogin()) {
            providers.put(1L, IdentityProvider.builder()
                    .id(1).name("Google account")
                    .url("https://accounts.google.com/")
                    .clientId("89170361789-mg8l3t3f11vo0cf0hce4h85epi0qqq3q.apps.googleusercontent.com")
                    .clientSecret("itCIp2JGR2NKBAu4Se9LCAjp")
                    .build()
            );
        }

        providers.put(2L, IdentityProvider.builder()
                .id(2).name("Kitos SSO")
                .url("https://os2saml.syddjurs.dk/")
                .clientId("suneclient")
                .clientSecret("secret")
                .build()
        );
    }

    public Optional<IdentityProvider> findProvider(long id) {
        return Optional.ofNullable(providers.get(id));
    }

    public Iterable<IdentityProvider> identityProviderList() {
        return providers.values();
    }

    public List<IdentityProviderPO> identityProviderPOList() {
        return StreamSupport.stream(identityProviderList().spliterator(), false)
                .map(IdentityProviderPO::new)
                .collect(Collectors.toList());
    }

}
