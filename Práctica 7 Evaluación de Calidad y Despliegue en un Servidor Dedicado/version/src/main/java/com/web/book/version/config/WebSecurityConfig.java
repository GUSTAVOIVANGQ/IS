package com.web.book.version.config;

import com.web.book.version.service.OAuth2UserService;
import com.web.book.version.service.UserDetailsServiceImpl;
import com.web.book.version.service.UserService; // Añadir este import

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final APIKeyConfig apiKeyConfig;
    private final UserService userService; // Añadir este campo

    @Autowired
    public WebSecurityConfig(
            UserDetailsServiceImpl userDetailsService,
            APIKeyConfig apiKeyConfig,
            @Lazy UserService userService) { // Añadir @Lazy aquí
        this.userDetailsService = userDetailsService;
        this.apiKeyConfig = apiKeyConfig;
        this.userService = userService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> {
                auth
                    .requestMatchers("/",
                               "/auth/**",
                               "/index",
                               "/css/**",
                               "/js/**",
                               "/images/**",
                               "/static/**",
                               "/uploads/**",
                               "/home", 
                               "/dashboard", 
                               "/index",
                               "/movies",
                               "/movies/search",
                               "/movies/details/**",
                               "/books",
                               "/books/search",
                               "/books/details/**",
                               // Nuevos endpoints públicos de la API
                               "/api/books/featured",
                               "/api/books/search",
                               "/api/shows/featured",  // Agregar nuevo endpoint público
                               "/api/shows/search").permitAll()  // Agregar nuevo endpoint público
                    .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                    .requestMatchers("/api/users").hasAuthority("ROLE_ADMIN") // Nueva regla para admin
                    .requestMatchers("/api/users/{id}").hasAuthority("ROLE_ADMIN") // Nueva regla para admin
                    .requestMatchers("/api/users/profile/**").permitAll() // Acceso autenticado al perfil
                    .requestMatchers("/api/books/**", "/api.shows/**").authenticated() // Proteger endpoints privados
                    .requestMatchers("/user/**").authenticated()
                    .requestMatchers("/history/**").authenticated()
                    .requestMatchers("/search/**").authenticated()
                    .requestMatchers("/recommendations/**").authenticated()
                    .requestMatchers("/history/admin/**").hasAuthority("ROLE_ADMIN")
                    .anyRequest().authenticated();
            })
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .successHandler(authenticationSuccessHandler())
                .permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exc -> exc
                .authenticationEntryPoint((request, response, authException) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.sendRedirect("/auth/login");
                    }
                })
            )
            .exceptionHandling(exc -> exc
                .authenticationEntryPoint((request, response, authException) -> {
                    // Check if it's an API request or accepts JSON
                    if (request.getRequestURI().startsWith("/api/") || 
                        request.getHeader("Accept").contains("application/json")) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.sendRedirect("/auth/login");
                    }
                })
            )
            .headers(headers -> headers
                .frameOptions().disable()
                .contentSecurityPolicy("img-src 'self' data: https://*.dropboxusercontent.com")
            );

        // Configurar OAuth2 solo si está habilitado
        if (apiKeyConfig != null && apiKeyConfig.isGoogleAuthEnabled()) {
            http.oauth2Login(oauth2 -> oauth2
                .loginPage("/auth/login")
                .successHandler(authenticationSuccessHandler())
                .failureUrl("/auth/login?error")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(new OAuth2UserService(userService)))
            );
        }

        // Agregar configuración para manejo de CORS
        http.cors(cors -> cors.configurationSource(request -> {
            var corsConfig = new org.springframework.web.cors.CorsConfiguration();
            corsConfig.setAllowedOrigins(java.util.List.of("*"));
            corsConfig.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            corsConfig.setAllowedHeaders(java.util.List.of("*"));
            corsConfig.setExposedHeaders(java.util.List.of("Authorization")); // Exponer header de autorización
            return corsConfig;
        }));
    
        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, 
                                             HttpServletResponse response, 
                                             Authentication authentication) throws IOException {
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                boolean isAdmin = authorities.stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
                
                if (isAdmin) {
                    response.sendRedirect("/admin/dashboard");
                } else {
                    response.sendRedirect("/home");
                }
            }
        };
    }
}