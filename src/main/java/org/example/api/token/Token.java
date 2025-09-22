package org.example.api.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.example.api.data.controllers.CustomerController;
import org.example.api.data.entity.Customer;
import org.example.api.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.util.Date;
import java.util.Optional;

@Component
public class Token {

    private static final String jwtSecret = "yourverylongseaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaacretkeythatisatleast64byteslong";
    private final long jwtExpirationInMs = 3600000; // Tiempo de expiración de 1 hora

    @Lazy
    @Autowired
    private CustomerService customerService;

    // Genera el token JWT
    public String generateToken(String customerEmail) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        Customer customer = customerService.findByEmail(customerEmail)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        String role = customer.getRole().name();

        return Jwts.builder()
                .setSubject(customerEmail)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    // Obtiene el email del cliente desde el token
    public static String getCustomerEmailFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
    // Obtiene el rol del cliente desde el token
    public static String getCustomerRoleFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.get("role", String.class);
    }

    // Valida el token JWT
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }


}
