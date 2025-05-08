package uk.co.obora.dcs.security;

import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static uk.co.obora.dcs.security.SecurityRoles.REGULAR_USER;

@Builder
@Data
public class DiscordIdentity implements OAuth2User {

    private Long discordId;
    private String username;
    private String discriminator;
    private String email;

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of(
            "id", getDiscordId(),
            "username", getUsername(),
            "discriminator", getDiscriminator(),
            "email", getEmail()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // TODO: Get the roles from the database
        return Collections.singleton(new SimpleGrantedAuthority(REGULAR_USER.toString()));
    }

    @Override
    public String getName() {
        return getUsername();
    }

    public static DiscordIdentity from(Map<String, Object> attributes) {
        Long id = Long.parseLong(getAttributeOrDefault(attributes, "id", "-1"));
        String name = getAttributeOrDefault(attributes, "global_name", getAttributeOrDefault(attributes, "username", ""));
        String email = getAttributeOrDefault(attributes, "email", "");

        if (id < 0 || name.isEmpty() || email.isEmpty()) {
            throw new IllegalArgumentException("Invalid attributes provided for user login. id: " + id + ", name: " + name + ", email: " + email);
        }

        return DiscordIdentity.builder()
            .discordId(id)
            .username(name)
            .discriminator(getAttributeOrDefault(attributes, "discriminator", "0"))
            .email(email)
            .build();
    }

    private static String getAttributeOrDefault(Map<String, Object> attributes, String key, Object defaultValue) {
        if (defaultValue == null) {
            throw new IllegalArgumentException("defaultValue cannot be null.");
        }
        Object value = attributes.computeIfAbsent(key, k -> defaultValue);
        return value.toString();
    }

}
