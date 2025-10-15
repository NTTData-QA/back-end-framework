package org.example.api.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.example.api.data.entity.Customer;
import org.example.api.data.repository.CustomerRepository;
import org.example.api.data.request.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;
import org.example.api.token.Token;
import java.util.Optional;

@Service
public class AuthService {


    @Autowired
    private Token token;

    private String jwtCookie = "cookieToken";

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    private Token tokenService; // Inyectamos el servicio de token


    public boolean authenticate(String email, String password) {
        Optional<Customer> customerOpt = customerRepository.findByEmail(email);

        if (customerOpt.isPresent()){
            Customer customer = customerOpt.get();
            return password.equals(customer.getPassword()) ;

        } else {
            return false;
        }
    }

    public ResponseCookie generateJwtCookie(LoginRequest loginRequest) {
        String jwt = tokenService.generateToken(loginRequest.getEmail());
        return ResponseCookie.from(jwtCookie, jwt).path("/").maxAge( 60 * 60).httpOnly(true).build();
    }

    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, jwtCookie);
        if (cookie != null) {
            return cookie.getValue();
        } else {
            return null;
        }
    }

    // Metodo para buscar el cliente por email
    public Optional<Customer> findCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }
    public Boolean AreYouLogged(HttpServletRequest request) {
        String jwt = getJwtFromCookies(request);

        return jwt == null || !token.validateToken(jwt);
    }
}

