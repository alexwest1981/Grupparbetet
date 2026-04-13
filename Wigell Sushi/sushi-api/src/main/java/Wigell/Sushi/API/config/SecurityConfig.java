package Wigell.Sushi.API.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionManagementPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                
                // Endast ADMIN får hantera rum och kunder
                .requestMatchers("/api/v1/rooms/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/customers/**").hasRole("ADMIN")
                
                // Rätter: Båda får läsa. Bara Admin får ändra/skapa.
                .requestMatchers(HttpMethod.GET, "/api/v1/dishes/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/v1/dishes/**").hasRole("ADMIN") // POST, PUT, DELETE
                
                // Bokningar: Båda får hantera (Kund sina egna, Admin alla)
                .requestMatchers("/api/v1/bookings/**").hasAnyRole("ADMIN", "USER")
                
                // Ordrar: Båda får hantera
                .requestMatchers("/api/v1/orders/**").hasAnyRole("ADMIN", "USER")
                
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        return new CustomJwtAuthenticationConverter();
    }
}
