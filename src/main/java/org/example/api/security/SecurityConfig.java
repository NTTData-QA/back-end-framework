package org.example.api.security;

import jakarta.ws.rs.HttpMethod;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter; // Asumo que este filtro se encarga de validar el token JWT.

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                // Configuración para permitir ciertas rutas sin verificación
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**","/api/logout").permitAll()
                        .requestMatchers(PathRequest.toH2Console()).permitAll()
                        //REGLAS POR ROL - SOLO PRUEBAS
                        .requestMatchers("api/customer/**").hasRole("ADMIN")
                        .requestMatchers(new RegexRequestMatcher("^/api/accounts/\\d+$", "GET")).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/accounts").hasAnyRole("USER","ADMIN")
                        .anyRequest().authenticated() // Permitir todas las solicitudes sin verificación de autenticación.
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Sin estado para evitar manejo de sesión
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Agregar el filtro de autenticación basado en JWT

        // Configuración para permitir la visualización del H2 en un frame
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        return http.build();
    }
}
