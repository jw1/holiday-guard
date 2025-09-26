package com.jw.holidayguard.security;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        // Custom login filter (POST /login only, no HTML page)
        UsernamePasswordAuthenticationFilter loginFilter =
                new UsernamePasswordAuthenticationFilter(authenticationManager);
        loginFilter.setFilterProcessesUrl("/api/login");
        loginFilter.setAuthenticationSuccessHandler((request, response, authentication) -> {
            response.setStatus(HttpServletResponse.SC_OK);
        });
        loginFilter.setAuthenticationFailureHandler((request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        });
        loginFilter.setSecurityContextRepository(new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        ));

        http
                .csrf(AbstractHttpConfigurer::disable) // SPA-friendly
                .authorizeHttpRequests(auth -> auth
                        // Publicly accessible frontend/static
                        .requestMatchers("/", "/index.html", "/vite.svg", "/assets/**").permitAll()

                        // Health checks / public API
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/v1/schedules/*/should-run").permitAll()

                        // Secure API endpoints
                        .requestMatchers(HttpMethod.GET, "/api/v1/schedules/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/schedules/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/schedules/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/audit-logs").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/v1/dashboard/**").authenticated()
                        .requestMatchers("/api/v1/user/principal").authenticated()

                        // Everything else requires auth
                        .anyRequest().authenticated()
                )

                // kill the default Spring login page
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())

                // add our JSON/AJAX login filter instead
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            logger.info("Received logout request for user {}", authentication.getName());
                        })
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    public UserDetailsService users() {
        return new InMemoryUserDetailsManager(
                User.withUsername("admin").password("admin").roles("ADMIN", "USER").build(),
                User.withUsername("user").password("user").roles("USER").build()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
