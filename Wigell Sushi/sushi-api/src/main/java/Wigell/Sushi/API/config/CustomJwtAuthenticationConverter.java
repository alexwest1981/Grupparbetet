package Wigell.Sushi.API.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        Collection<GrantedAuthority> authorities = List.of();
        
        // Keycloak lägger sina roller under "realm_access" -> "roles"
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            authorities = ((List<String>) realmAccess.get("roles")).stream()
                    .map(roleName -> "ROLE_" + roleName.toUpperCase()) // Spring Security förväntar sig prefixet ROLE_
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        
        return new JwtAuthenticationToken(jwt, authorities);
    }
}
