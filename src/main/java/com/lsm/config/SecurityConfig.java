package com.lsm.config;

import com.lsm.service.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.lsm.security.JwtAuthenticationFilter;
import com.lsm.service.AppUserService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@PropertySource("classpath:application.properties")
@Slf4j
public class SecurityConfig {

    private final AppUserService appUserService;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Value("${cors.allowed-methods}")
    private List<String> allowedMethods;

    @Value("${cors.allowed-headers}")
    private List<String> allowedHeaders;

    @Value("${cors.max-age}")
    private Long maxAge;

    @Bean
    public HttpSessionSecurityContextRepository httpSessionSecurityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(
                jwtTokenProvider,
                appUserService,
                handlerExceptionResolver,
                httpSessionSecurityContextRepository()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource())
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.getWriter().write("Access Denied");
                        })
                )
                .authorizeHttpRequests(registry -> {
                    // Public endpoints (TODO: register must restricted for non-admin users after production.)
                    registry.requestMatchers(
                            "/api/v1/auth/register",
                            "/api/v1/auth/login",
                            "/api/v1/auth/refresh",
                            "/health",
                            "/health/**",
                            "/actuator/**"
                    ).permitAll();

                    // Swagger/OpenAPI endpoints
                    registry.requestMatchers(
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/api-docs/**"
                    ).permitAll();

                    // Static resources
                    registry.requestMatchers(
                            "/css/**",
                            "/js/**",
                            "/images/**",
                            "/favicon.ico"
                    ).permitAll();

                    // Class endpoints
                    registry.requestMatchers(HttpMethod.POST, "/api/v1/classes/**")
                            .hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");

                    registry.requestMatchers(HttpMethod.GET, "/api/v1/classes/**")
                            .hasAnyAuthority("ROLE_TEACHER", "ROLE_STUDENT", "ROLE_ADMIN", "ROLE_COORDINATOR");

                    registry.requestMatchers(HttpMethod.PUT, "/api/v1/classes/**")
                            .hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");

                    registry.requestMatchers(HttpMethod.DELETE, "/api/v1/classes/**")
                            .hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");

                    // Student management in classes
                    registry.requestMatchers(HttpMethod.POST, "/api/v1/classes/*/students/*")
                            .hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");

                    registry.requestMatchers(HttpMethod.DELETE, "/api/v1/classes/*/students/*")
                            .hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");

                    // Assignment endpoints
                    registry.requestMatchers(HttpMethod.POST, "/api/v1/assignments/**")
                            .hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");
                    registry.requestMatchers(HttpMethod.GET, "/api/v1/assignments/student/**")
                            .hasAnyAuthority("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");
                    registry.requestMatchers(HttpMethod.PUT, "/api/v1/assignments/**")
                            .hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");
                    registry.requestMatchers(HttpMethod.DELETE, "/api/v1/assignments/**")
                            .hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");
                    registry.requestMatchers(HttpMethod.PATCH, "/api/v1/assignments/**")
                            .hasAnyAuthority("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");

                    // Attendance endpoints
                    registry.requestMatchers(HttpMethod.POST, "/api/v1/attendance/**")
                            .hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");
                    registry.requestMatchers(HttpMethod.GET, "/api/v1/attendance/**")
                            .hasAnyAuthority("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");
                    registry.requestMatchers(HttpMethod.PATCH, "/api/v1/attendance/**")
                            .hasAnyAuthority("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");
                    registry.requestMatchers(HttpMethod.DELETE, "/api/v1/attendance/**")
                            .hasAnyAuthority("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");

                    // Announcements endpoints
                    registry.requestMatchers(HttpMethod.POST, "/api/v1/announcements/")
                            .hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");
                    registry.requestMatchers(HttpMethod.GET, "/api/v1/announcements/**")
                            .hasAnyAuthority("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");
                    registry.requestMatchers(HttpMethod.PUT, "/api/v1/announcements/{id}")
                            .hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");
                    registry.requestMatchers(HttpMethod.DELETE, "/api/v1/announcements/{id}")
                            .hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");

                    registry.requestMatchers(HttpMethod.GET, "/api/v1/users/**")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_COORDINATOR");
                    registry.requestMatchers(HttpMethod.PUT, "/api/v1/users/**")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_COORDINATOR");
                    registry.requestMatchers(HttpMethod.DELETE, "/api/v1/users/**")
                            .hasAuthority("ROLE_ADMIN");

                    // Student management endpoints
                    registry.requestMatchers(HttpMethod.GET, "/api/v1/students/**")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_TEACHER", "ROLE_COORDINATOR");
                    registry.requestMatchers(HttpMethod.PUT, "/api/v1/students/**")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_COORDINATOR");
                    registry.requestMatchers(HttpMethod.DELETE, "/api/v1/students/**")
                            .hasAuthority("ROLE_ADMIN");

                    // Teacher management endpoints
                    registry.requestMatchers(HttpMethod.GET, "/api/v1/teachers/**")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_COORDINATOR");
                    registry.requestMatchers(HttpMethod.PUT, "/api/v1/teachers/**")
                            .hasAnyAuthority("ROLE_ADMIN", "ROLE_COORDINATOR");
                    registry.requestMatchers(HttpMethod.DELETE, "/api/v1/teachers/**")
                            .hasAuthority("ROLE_ADMIN");

                    // Past exam endpoints
                    registry.requestMatchers(HttpMethod.POST, "/api/v1/past-exams/**")
                            .hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");
                    registry.requestMatchers(HttpMethod.GET, "/api/v1/past-exams/student/**")
                            .hasAnyAuthority("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");
                    registry.requestMatchers(HttpMethod.GET, "/api/v1/past-exams/**")
                            .hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");
                    registry.requestMatchers(HttpMethod.PUT, "/api/v1/past-exams/**")
                            .hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");
                    registry.requestMatchers(HttpMethod.DELETE, "/api/v1/past-exams/**")
                            .hasAnyAuthority("ROLE_TEACHER", "ROLE_ADMIN", "ROLE_COORDINATOR");


                    // Default policy
                    registry.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .xssProtection(
                                xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                        ).contentSecurityPolicy(
                                cps -> cps.policyDirectives("script-src 'self'")
                        )
                        .contentSecurityPolicy(csp ->
                                csp.policyDirectives("default-src 'self'; frame-ancestors 'none';"))
                )
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpStatus.OK.value());
                        })
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(allowedMethods);
        configuration.setAllowedHeaders(allowedHeaders);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}