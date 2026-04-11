package com.wigell.dashboard.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String adminContextPath;

    public SecurityConfig(AdminServerProperties adminServerProperties) {
        this.adminContextPath = adminServerProperties.getContextPath();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests((authorizeRequests) ->
                authorizeRequests
                    .requestMatchers(adminContextPath + "/assets/**").permitAll()
                    .requestMatchers(adminContextPath + "/login").permitAll()
                    .requestMatchers(adminContextPath + "/actuator/**").permitAll() 
                    .requestMatchers(adminContextPath + "/instances", adminContextPath + "/instances/**").permitAll()
                    .anyRequest().authenticated()
            )
            // Aktiverar OIDC / OAuth2 inloggning (Skickar användaren till Keycloak-skärmen)
            .oauth2Login(Customizer.withDefaults())
            
            // Tillåter API-anrop med Bearer token (om något annat system anropar dashboarden)
            .oauth2ResourceServer((oauth2) -> oauth2
                .jwt(Customizer.withDefaults())
            )
            .logout((logout) -> logout
                .logoutUrl(adminContextPath + "/logout")
            )
            .csrf((csrf) -> csrf
                .ignoringRequestMatchers(adminContextPath + "/logout", adminContextPath + "/actuator/**", adminContextPath + "/instances", adminContextPath + "/instances/**")
            );

        return http.build();
    }
}
