package uk.co.obora.dcs.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

public class DiscordAuthorizationCodeTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> requestEntityConverter = new DiscordRequestEntityConverter(new OAuth2AuthorizationCodeGrantRequestEntityConverter());
    private RestTemplate oauthRestTemplate;

    public DiscordAuthorizationCodeTokenResponseClient() {
        RestTemplate restTemplate = new RestTemplate(Arrays.asList(new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter()));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        this.oauthRestTemplate = restTemplate;
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
        Assert.notNull(authorizationGrantRequest, "authorizationCodeGrantRequest cannot be null");
        RequestEntity<?> request = this.requestEntityConverter.convert(authorizationGrantRequest);
        ResponseEntity<OAuth2AccessTokenResponse> response = this.getResponse(request);
        OAuth2AccessTokenResponse tokenResponse = response.getBody();
        Assert.notNull(tokenResponse, "The authorization server responded to this Authorization Code grant request with an empty body; as such, it cannot be materialized into an OAuth2AccessTokenResponse instance. Please check the HTTP response code in your server logs for more details.");
        return tokenResponse;
    }

    private ResponseEntity<OAuth2AccessTokenResponse> getResponse(RequestEntity<?> request) {
        try {
            return this.oauthRestTemplate.exchange(request, OAuth2AccessTokenResponse.class);
        } catch (RestClientException ex) {
            OAuth2Error oauth2Error = new OAuth2Error("invalid_token_response", "An error occurred while attempting to retrieve the OAuth 2.0 Access Token Response: " + ex.getMessage(), (String)null);
            throw new OAuth2AuthorizationException(oauth2Error, ex);
        }
    }
}
