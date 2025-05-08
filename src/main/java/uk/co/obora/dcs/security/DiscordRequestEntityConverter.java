package uk.co.obora.dcs.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

public class DiscordRequestEntityConverter <T extends AbstractOAuth2AuthorizationGrantRequest> implements Converter<T, RequestEntity<?>> {

    private static final String PERMISSIBLE_CLIENT_REGISTRATION = "discord";
    private static final String PERMISSIBLE_CLIENT_AUTHENTICATION_METHOD = "POST";

    private final Converter<T, RequestEntity<?>> delegate;

    DiscordRequestEntityConverter(Converter<T, RequestEntity<?>> delegate) {
        this.delegate = delegate;
    }

    @Override
    public RequestEntity<?> convert(T request) {
        ClientRegistration clientRegistration = request.getClientRegistration();
        String registrationId = clientRegistration.getRegistrationId();
        String clientAuthenticationMethod = clientRegistration.getClientAuthenticationMethod().getValue();
        if (!PERMISSIBLE_CLIENT_REGISTRATION.equalsIgnoreCase(registrationId)) {
            throw new IllegalArgumentException("This class only supports the client registration with id [" + PERMISSIBLE_CLIENT_REGISTRATION + "]. Please use a different EntityConverter.");
        }
        if (!PERMISSIBLE_CLIENT_AUTHENTICATION_METHOD.equalsIgnoreCase(clientAuthenticationMethod)) {
            throw new IllegalArgumentException("This class only supports the client authentication method [" + PERMISSIBLE_CLIENT_AUTHENTICATION_METHOD + "]. Please use a different EntityConverter.");
        }
        return this.delegate.convert(request);
    }
}
