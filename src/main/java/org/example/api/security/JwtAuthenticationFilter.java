package org.example.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.api.data.repository.CustomerRepository;
import org.example.api.service.AuthService;
import org.example.api.token.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.example.api.data.entity.Customer;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AuthService authService;

    @Autowired
    private Token token;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        //Obtiene el JWT de la cookie
        String jwt = authService.getJwtFromCookies(request);
        String rfr = authService.getRfrFromCookies(request);

        if (jwt != null && token.validateToken(jwt)) {
            // Si el token es válido, permite la ejecución de la petición
            String email = Token.getCustomerEmailFromJWT(jwt);
            String role = Token.getCustomerRoleFromJWT(jwt);

            //Verifica si el cliente esta autenticado o no
            if(SecurityContextHolder.getContext().getAuthentication() == null){
                Optional<Customer> optionalCustomer = customerRepository.findByEmail(email);

                if (optionalCustomer.isPresent()){
                    Customer customer = optionalCustomer.get();
                    String authorityName = "ROLE_" + (role == null ? "USER" : role.toUpperCase());

                    UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                            customer.getCustomerId().toString(),
                            customer.getPassword(),
                            Collections.singletonList(new SimpleGrantedAuthority(authorityName)));

                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

                } else {
                    response.sendError(HttpStatus.UNAUTHORIZED.value(), "Cliente no encontrado");
                    return;
                }
            }
        } else if (jwt != null && !jwt.isEmpty()){
            response.sendError(HttpStatus.UNAUTHORIZED.value(),"JWT no valido");
            return;
        }
        //Continua con los filtros
        filterChain.doFilter(request, response);
    }
}
