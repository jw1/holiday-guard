package com.jw.holidayguard.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;

/**
 * Security configuration for the application.
 *
 * <p>This configuration is designed as a placeholder setup for local development
 * and simple in-memory authentication. It provides:
 * <ul>
 *   <li>A JSON-based login endpoint at <code>/api/login</code> that accepts username/password
 *       credentials instead of Spring’s default HTML login form.</li>
 *   <li>A logout endpoint at <code>/api/logout</code> returning HTTP 200 on success.</li>
 *   <li>Role-based access control rules for selected API endpoints.</li>
 *   <li>401 Unauthorized responses when no authentication is present.</li>
 * </ul>
 *
 * <p>Key differences from the defaults:
 * <ul>
 *   <li><code>formLogin()</code> and <code>httpBasic()</code> are disabled to avoid conflicts
 *       with the SPA frontend’s own <code>/login</code> route.</li>
 *   <li>In-memory users are defined here with hard-coded usernames and roles.</li>
 *   <li>No password encoding is applied (NoOp encoder), since this is not meant for production.</li>
 * </ul>
 *
 * <p>Intended for replacement with a real authentication mechanism such as
 * OAuth2, JWT, or a centralized identity provider. The structure is kept close
 * to a production setup so that swapping implementations later is straightforward.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {

        // custom login filter (POST /login only, no HTML page)
        UsernamePasswordAuthenticationFilter loginFilter = authenticationFilter(authenticationManager);

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

                // frontend expects 401s instead of 403s
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )

                // kill the default Spring login page
                .formLogin(AbstractHttpConfigurer::disable)

                // add our JSON/AJAX login filter instead
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
                );

        return http.build();
    }

    private static UsernamePasswordAuthenticationFilter authenticationFilter(AuthenticationManager authenticationManager) {
        var loginFilter = new UsernamePasswordAuthenticationFilter(authenticationManager);
        loginFilter.setFilterProcessesUrl("/api/login");
        loginFilter.setAuthenticationSuccessHandler((request, response, authentication) -> response.setStatus(HttpServletResponse.SC_OK));
        loginFilter.setAuthenticationFailureHandler((request, response, exception) -> response.setStatus(HttpServletResponse.SC_UNAUTHORIZED));
        loginFilter.setSecurityContextRepository(new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        ));
        return loginFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    /**
     * Just some default users -- it is expected the end user will swap in
     * a different authentication system
     *
     * @return simple in-memory user store
     */
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
