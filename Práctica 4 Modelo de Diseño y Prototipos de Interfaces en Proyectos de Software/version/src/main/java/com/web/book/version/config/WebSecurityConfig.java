package com.web.book.version.config;

import com.web.book.version.service.UserDetailsServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private OAuth2Config oauth2Config;

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
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, 
                                             HttpServletResponse response, 
                                             Authentication authentication) throws IOException {
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                for (GrantedAuthority authority : authorities) {
                    if (authority.getAuthority().equals("ROLE_ADMIN")) {
                        response.sendRedirect("/admin/dashboard");
                        return;
                    }
                }
                response.sendRedirect("/home");
            }
        };
    }

    @Autowired
    public WebSecurityConfig(
        UserDetailsServiceImpl userDetailsService,
        OAuth2Config oauth2Config) {
        this.userDetailsService = userDetailsService;
        this.oauth2Config = oauth2Config;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/",
                               "/auth/**",
                               "/index",
                               "/css/**",
                               "/js/**",
                               "/images/**",
                               "/static/**",
                               "/history/**",
                               "/home", 
                               "/dashboard", 
                               "/index",
                               "/movies",           // Permitir acceso a la página de películas
                               "/movies/search",    // Permitir búsquedas sin autenticar
                               "/movies/details/**",
                               "/books",
                               "/books/search",
                               "/books/details/**").permitAll()
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/user/**").authenticated()
//                .requestMatchers("/home/**").authenticated()
                .requestMatchers("/history/**").authenticated()
                .requestMatchers("/search/**").authenticated()
                .requestMatchers("/recommendations/**").authenticated() // Agregar esta línea
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .defaultSuccessUrl("/home", true) // Cambia esto a /dashboard
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
            .loginPage("/auth/login")
            .defaultSuccessUrl("/home", true)
            .failureUrl("/auth/login?error")
            .userInfoEndpoint(userInfo -> userInfo
                .userService(oauth2Config.oauth2UserService()))
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/auth/login?logout")
                .permitAll()
            );
    
        return http.build();
    }
}