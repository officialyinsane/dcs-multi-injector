package uk.co.obora.dcs.security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.springframework.http.HttpMethod.GET;

@Service
@RequiredArgsConstructor
public class DiscordIdentityService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final RestTemplate restTemplate;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userRequest.getAccessToken().getTokenValue());

        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<Map<String, Object>> response = exchange(
            userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUri(),
            entity);

        Map<String, Object> attributes = response.getBody();
        if (attributes != null) {
            // TODO: Verify the user exists in the database - create if not (first user is Administrator)
            return DiscordIdentity.from(attributes);
        }

        return null;
    }

    private ResponseEntity<Map<String, Object>> exchange(String uri, HttpEntity<?> entity) {
        return restTemplate.exchange(uri, GET, entity, new ParameterizedTypeReference<>() {});
    }
}
