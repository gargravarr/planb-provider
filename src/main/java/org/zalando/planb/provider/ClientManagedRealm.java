package org.zalando.planb.provider;

import org.zalando.planb.provider.api.Client;

import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

public interface ClientManagedRealm extends ClientRealm {

    @Override
    default void authenticate(String clientId, String clientSecret, Set<String> scopes, Set<String> defaultScopes)
            throws ClientRealmAuthenticationException, ClientRealmAuthorizationException {
        final Client client = get(clientId)
                .orElseThrow(() -> new ClientRealmAuthenticationException(clientId, getName()));

        // TODO hardcoded assumption, that only Resource Owner Password Credentials flow is supported
        if (!client.getIsConfidential()) {
            throw new ClientRealmAuthenticationException(clientId, getName());
        }


        if (!Realm.checkBCryptPassword(clientSecret, client.getSecretHash())) {
            throw new ClientRealmAuthenticationException(clientId, getName());
        }

        final Set<String> missingScopes = scopes.stream()
                .filter(scope -> !defaultScopes.contains(scope))
                .filter(scope -> !client.getScopes().contains(scope))
                .collect(toSet());

        if (!missingScopes.isEmpty()) {
            throw new ClientRealmAuthorizationException(clientId, getName(), missingScopes);
        }
    }

    default void update(String clientId, Client data) throws NotFoundException {
        final Client existing = get(clientId).orElseThrow(() -> new NotFoundException(format("Could not find client %s in realm %s", clientId, getName())));

        final Client update = new Client();
        update.setSecretHash(Optional.ofNullable(data.getSecretHash()).orElseGet(existing::getSecretHash));
        update.setScopes(Optional.ofNullable(data.getScopes()).filter(set -> !set.isEmpty()).orElseGet(existing::getScopes));
        update.setIsConfidential(Optional.ofNullable(data.getIsConfidential()).orElseGet(existing::getIsConfidential));

        createOrReplace(clientId, update);
    }

    void delete(String clientId) throws NotFoundException;

    void createOrReplace(String id, Client client);

    Optional<Client> get(String clientId);
}
