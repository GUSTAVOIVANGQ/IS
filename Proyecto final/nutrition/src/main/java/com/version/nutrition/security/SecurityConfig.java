package com.version.nutrition.security;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

            String redirectUrl;
            if (roles.contains("ROLE_NUTRICIONISTA")) {
                redirectUrl = "/nutricionista/dashboard";
            } else if (roles.contains("ROLE_PACIENTE")) {
                redirectUrl = "/paciente/dashboard";
            } else {
                redirectUrl = "/";
            }
            
            response.sendRedirect(redirectUrl);
        };
    }
        
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Recursos públicos
                .requestMatchers(
                    "/",
                    "/registro/**",
                    "/auth/**",
                    "/recuperar-password/**",
                    "/reset-password/**",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/uploads/**",
                    "/webjars/**",
                    "/assets/**",
                    "/fonts/**",
                    "/api/public/**"
                ).permitAll()
                
                // Rutas específicas por rol
                .requestMatchers("/login", "/registro/**", "/recuperar-password").permitAll()
                
                // Rutas para gestión de pacientes por nutricionista
                .requestMatchers("/nutricionista/pacientes/nuevo").hasRole("NUTRICIONISTA")
                .requestMatchers("/nutricionista/pacientes/crear").hasRole("NUTRICIONISTA")
                .requestMatchers("/nutricionista/pacientes/*/editar").hasRole("NUTRICIONISTA")
                .requestMatchers("/nutricionista/pacientes/*/eliminar").hasRole("NUTRICIONISTA")
                
                // Nuevas rutas de seguimiento para pacientes
                .requestMatchers("/paciente/seguimiento/**").hasRole("PACIENTE")
                .requestMatchers("/paciente/seguimiento/registrar").hasRole("PACIENTE")
                .requestMatchers("/paciente/seguimiento/historial").hasRole("PACIENTE")
                .requestMatchers("/paciente/seguimiento/actividad").hasRole("PACIENTE")
                .requestMatchers("/paciente/seguimiento/reporte").hasRole("PACIENTE")
                .requestMatchers("/api/seguimiento/**").hasRole("PACIENTE")
                
                // Rutas existentes
                .requestMatchers("/paciente/**").hasRole("PACIENTE")
                .requestMatchers("/nutricionista/**").hasRole("NUTRICIONISTA")
                .requestMatchers("/nutricionista/pacientes/**").hasRole("NUTRICIONISTA")
                .requestMatchers("/nutricionista/planes/**").hasRole("NUTRICIONISTA")
                .requestMatchers("/nutricionista/planes/crear/**").hasRole("NUTRICIONISTA")
                .requestMatchers("/nutricionista/planes/editar/**").hasRole("NUTRICIONISTA")
                .requestMatchers("/nutricionista/planes/generar-automatico/**").hasRole("NUTRICIONISTA")
                .requestMatchers("/nutricionista/planes/*/agregar-alimento").hasRole("NUTRICIONISTA")
                .requestMatchers("/paciente/planes/**").hasRole("PACIENTE")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Agregar rutas para sistema de tickets
                .requestMatchers("/paciente/tickets/**").hasRole("PACIENTE")
                .requestMatchers("/paciente/tickets/crear").hasRole("PACIENTE")
                .requestMatchers("/paciente/tickets/*/responder").hasRole("PACIENTE")
                .requestMatchers("/paciente/tickets/*/resolver").hasRole("PACIENTE")
                .requestMatchers("/paciente/tickets/*/cerrar").hasRole("PACIENTE")
                .requestMatchers("/paciente/tickets/*/etiquetas").hasRole("PACIENTE")
                .requestMatchers("/nutricionista/tickets/**").hasRole("NUTRICIONISTA")
                
                // APIs relacionadas con planes
                .requestMatchers("/api/planes/**").authenticated()
                .requestMatchers("/api/alimentos/**").authenticated()
                .requestMatchers("/api/seguimiento/**").authenticated()
                
                // APIs relacionadas con tickets
                .requestMatchers("/api/tickets/**").permitAll()
                .requestMatchers("/api/tickets").permitAll()
                .requestMatchers("/api/tickets/paciente/**").permitAll()
                .requestMatchers("/api/tickets/{id}/**").permitAll()
                .requestMatchers("/api/tickets/{id}/estado").permitAll()
                .requestMatchers("/api/tickets/{id}/etiquetas").permitAll()
                
                // Rutas para perfil de paciente
                .requestMatchers("/paciente/profile/**").hasRole("PACIENTE")
                .requestMatchers("/uploads/profile-photos/**").permitAll()
                
                // Rutas para perfil de nutricionista
                .requestMatchers("/nutricionista/perfil").hasRole("NUTRICIONISTA")
                .requestMatchers("/nutricionista/perfil/**").hasRole("NUTRICIONISTA")
                
                // Nuevos endpoints REST para citas
                .requestMatchers("/api/consultas/**").authenticated()
                .requestMatchers("/api/consultas").hasAnyRole("NUTRICIONISTA", "PACIENTE")
                .requestMatchers("/api/consultas/{id}").hasAnyRole("NUTRICIONISTA", "PACIENTE")
                
                // Todas las demás rutas requieren autenticación
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(customAuthenticationSuccessHandler()) // Usar el handler personalizado
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400) // 24 horas
                .rememberMeParameter("remember-me")
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", 
                                       "/nutricionista/planes/*/agregar-alimento",
                                       "/nutricionista/pacientes/nuevo",  // Add this line
                                       "/nutricionista/pacientes/filtrar",  // Agregar esta línea
                                       "/paciente/seguimiento/actividad",  // Agregar endpoint de actividad
                                       "/paciente/tickets/*/responder", // Agregar endpoint de respuestas
                                       "/api/consultas/**", // Agregar excepción CSRF para endpoints de citas
                                       // Agregar excepción CSRF para endpoints de tickets
                                       "/api/tickets/**")
            );

        return http.build();
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
    }
}
