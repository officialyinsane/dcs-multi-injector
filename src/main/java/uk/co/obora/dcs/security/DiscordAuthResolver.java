package uk.co.obora.dcs.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.security.oauth2.core.endpoint.PkceParameterNames.*;

public class DiscordAuthResolver implements OAuth2AuthorizationRequestResolver {

    private final StringKeyGenerator keyGenerator =  new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 96);
    private final OAuth2AuthorizationRequestResolver resolver;

    public DiscordAuthResolver(OAuth2AuthorizationRequestResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return createRequest(resolver.resolve(request));
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return createRequest(resolver.resolve(request, clientRegistrationId));
    }

    private OAuth2AuthorizationRequest createRequest(OAuth2AuthorizationRequest request) {
        if (request == null) {
            return null;
        }

        String key = keyGenerator.generateKey();
        Map<String, Object> attributes = new HashMap<>(request.getAttributes());
        attributes.putAll(getPkceAttributes(key));

        Map<String, Object> additionalParameters = new HashMap<>(request.getAdditionalParameters());
        additionalParameters.putAll(getPkceParameters(key));

        return OAuth2AuthorizationRequest.from(request)
            .attributes(attributes)
            .additionalParameters(additionalParameters)
            .build();
    }

    private Map<String, Object> getPkceAttributes(String verifier) {
        return Map.of(
            CODE_VERIFIER, verifier
        );
    }

    private Map<String, Object> getPkceParameters(String verifier) {
        try {
            return Map.of(
                CODE_CHALLENGE, createChallenge(verifier),
                CODE_CHALLENGE_METHOD, "S256"
            );
        } catch (NoSuchAlgorithmException e) {
            return Map.of(
                CODE_CHALLENGE, verifier
            );
        }
    }

    private static String createChallenge(String key) throws NoSuchAlgorithmException {
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(UTF_8)));
    }
}
