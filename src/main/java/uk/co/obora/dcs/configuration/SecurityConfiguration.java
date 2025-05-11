package uk.co.obora.dcs.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.oauth2.client.endpoint.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import uk.co.obora.dcs.security.DiscordAuthResolver;
import uk.co.obora.dcs.security.DiscordAuthorizationCodeTokenResponseClient;

import static org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final String DEFAULT_SUCCESS_URL = "/";

    private final DiscordAuthResolver discordAuthResolver;

    @Value("${vaadin.servlet.production.mode:false}")
    private boolean productionMode;

    @Autowired
    public SecurityConfiguration(ClientRegistrationRepository repository) {
        discordAuthResolver = new DiscordAuthResolver(
            new DefaultOAuth2AuthorizationRequestResolver(repository, DEFAULT_AUTHORIZATION_REQUEST_BASE_URI));
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, OAuth2UserService<OAuth2UserRequest, OAuth2User> discordIdentityService) throws Exception {
        HttpSecurity security = httpSecurity.authorizeHttpRequests(requests -> requests
            // TODO: Adding the below rules allows unauthenticated users to browse the whole page
               /* .requestMatchers(new AntPathRequestMatcher("/")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/sw.js")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/VAADIN/**")).permitAll()*/
                .requestMatchers(new AntPathRequestMatcher("/oauth2/**")).permitAll()
                .anyRequest()
                .authenticated())
            .oauth2Login(configurer -> configurer
                .defaultSuccessUrl(DEFAULT_SUCCESS_URL)
                .authorizationEndpoint(customizer -> customizer.authorizationRequestResolver(discordAuthResolver))
                .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(discordIdentityService))
                .tokenEndpoint(tokenEndpoint -> tokenEndpoint.accessTokenResponseClient(accessTokenResponseClient()))
            )
            .logout(LogoutConfigurer::permitAll);

        security = productionMode
           ? security.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
           : security.csrf(AbstractHttpConfigurer::disable);
        return security.build();
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        return new DiscordAuthorizationCodeTokenResponseClient();
    }
}
