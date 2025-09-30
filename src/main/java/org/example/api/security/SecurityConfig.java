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
                        //endpoints públicos
                        .requestMatchers("/public/**","/public/logout").permitAll()
                        .requestMatchers(PathRequest.toH2Console()).permitAll()
                        //REGLAS POR ROL
                        .requestMatchers(new RegexRequestMatcher("^/api/customer/\\d+$", "GET")).hasRole("ADMIN")
                        .requestMatchers(new RegexRequestMatcher("^/api/customer/email/[^/]+$", "GET")).hasRole("ADMIN")
                        .requestMatchers(new RegexRequestMatcher("^/api/account/\\d+$", "GET")).hasRole("ADMIN")
                        .requestMatchers(new RegexRequestMatcher("^/api/accounts/\\d+$", "GET")).hasRole("ADMIN")
                        .requestMatchers(new RegexRequestMatcher("^/api/amount/\\d+$", "GET")).hasRole("ADMIN")
                        .requestMatchers(new RegexRequestMatcher("^/api/card/\\d+$", "GET")).hasRole("ADMIN")
                        .requestMatchers(new RegexRequestMatcher("^/api/cards/\\d+$", "GET")).hasRole("ADMIN")
                        .requestMatchers(new RegexRequestMatcher("^/api/transfers/received/\\d+$", "GET")).hasRole("ADMIN")
                        .requestMatchers(new RegexRequestMatcher("^/api/transfers/sent/\\d+$", "GET")).hasRole("ADMIN")
                        .requestMatchers(new RegexRequestMatcher("^/api/transfers/history/\\d+$", "GET")).hasAnyRole("USER","ADMIN")
                        .requestMatchers(new RegexRequestMatcher("^/api/transfer/\\d+$", "GET")).hasAnyRole("USER","ADMIN")
                        .requestMatchers(new RegexRequestMatcher("^/api/account/deposit/\\d+$", "PATCH")).hasAnyRole("USER","ADMIN")
                        .requestMatchers(new RegexRequestMatcher("^/api/account/withdraw/\\d+$", "PATCH")).hasAnyRole("USER","ADMIN")
                        .requestMatchers(new RegexRequestMatcher("^/api/customer/[^/]+$", "DELETE")).hasRole("ADMIN")
                        .requestMatchers(new RegexRequestMatcher("^/api/account/delete/\\d+$", "DELETE")).hasRole("ADMIN")
                        .requestMatchers(new RegexRequestMatcher("^/api/account/delete/customer/\\d+$", "DELETE")).hasRole("ADMIN")
                        .requestMatchers(new RegexRequestMatcher("^/api/card/delete/\\d+$", "DELETE")).hasRole("ADMIN")
                        .requestMatchers(new RegexRequestMatcher("^/api/card/delete/account/\\d+$", "DELETE")).hasRole("ADMIN")
                        .requestMatchers(new RegexRequestMatcher("^/api/card/delete/customer/\\d+$", "DELETE")).hasRole("ADMIN")
                        .requestMatchers(new RegexRequestMatcher("^/api/transfer/\\d+$", "DELETE")).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/customers").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/customer").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/accounts").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/accounts/amount").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/BDcards").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/cards").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/transfers/received/all").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/account/new").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/card/new").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/transfer/new").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/customer/update/email").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/customer/update/password").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/customer/update/nameandsurname").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/account/delete").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/card/delete").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/admin/register").hasRole("ADMIN")

                        .anyRequest().authenticated() // Permitir todas las solicitudes sin verificación de autenticación.
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Sin estado para evitar manejo de sesión
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Agregar el filtro de autenticación basado en JWT

        // Configuración para permitir la visualización del H2 en un frame
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        return http.build();
    }
}
