package com.epam.gymcrmspringboot.config;

import com.epam.gymcrmspringboot.handler.RestAccessDeniedHandler;
import com.epam.gymcrmspringboot.handler.RestAuthenticationEntryPoint;
import com.epam.gymcrmspringboot.service.AuthenticationService;
import com.epam.gymcrmspringboot.service.JwtTokenRevocationService;
import com.epam.gymcrmspringboot.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtTokenRevocationService jwtTokenRevocationService;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final CorsProperties corsProperties;
    private final AuthenticationService authenticationService;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/trainees", "/trainers", "/authentication/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/trainings").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.GET, "/trainings/trainers/**").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.GET, "/trainings/trainees/**").hasRole("TRAINEE")
                        .requestMatchers(HttpMethod.GET, "/training-types/**").hasAnyRole("TRAINER", "TRAINEE")
                        .requestMatchers("/trainers/**").hasRole("TRAINER")
                        .requestMatchers("/trainees/**").hasRole("TRAINEE")
                        .requestMatchers(HttpMethod.PUT, "/users/password").hasAnyRole("TRAINER", "TRAINEE")
                        .anyRequest().authenticated())
                .logout(logout -> logout
                        .logoutUrl("/authentication/logout")
                        .addLogoutHandler((request, response, authentication) -> {
                            log.info("Logout request received. Revoking JWT token and clearing security context.");
                            String authorizationHeader = request.getHeader("Authorization");
                            String token = authenticationService.extractTokenFromAuthorizationHeader(authorizationHeader);
                            if (token != null) {
                                jwtTokenRevocationService.revokeToken(token);
                            }
                            SecurityContextHolder.clearContext();
                        })
                        .addLogoutHandler(new SecurityContextLogoutHandler())
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_OK)))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        if (corsProperties.getAllowedOrigins().isEmpty()) {
            // No frontend domain exists yet, so deny all cross-origin browser access by default.
            configuration.setAllowedOrigins(Collections.emptyList());
        } else {
            configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        }

        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setMaxAge(corsProperties.getMaxAgeSeconds());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
