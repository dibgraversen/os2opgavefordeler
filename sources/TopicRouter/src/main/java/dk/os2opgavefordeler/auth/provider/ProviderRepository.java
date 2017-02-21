package dk.os2opgavefordeler.auth.provider;

import dk.os2opgavefordeler.model.IdentityProvider;
import dk.os2opgavefordeler.model.presentation.IdentityProviderPO;
import dk.os2opgavefordeler.service.ConfigService;
import org.slf4j.Logger;

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
        String clientId = configService.getClientId();
        String clientSecret = configService.getClientSecret();
        providers.put(2L, IdentityProvider.builder()
                .id(2).name("OS2 SSO")
                .url("https://os2sso.miracle.dk/")
                .clientId(clientId)
                .clientSecret(clientSecret)
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
