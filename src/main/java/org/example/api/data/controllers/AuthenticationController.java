package org.example.api.data.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.api.data.entity.Customer;
import org.example.api.data.entity.Customer.UserType;
import org.example.api.data.request.LoginRequest;
import org.example.api.service.AuthService;
import org.example.api.service.CustomerService;
import org.example.api.token.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AuthenticationController {
    @Autowired
    private AuthService authService;
    @Autowired
    private Token token;

    private final CustomerService customerService;

    private final String jwtCookie = "cookieToken";

    public AuthenticationController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/public/register")
    public ResponseEntity<String> addCustomer(@Valid @RequestBody Customer nuevoCust) {
        try {
            nuevoCust.setRole(UserType.USER);
            customerService.register(nuevoCust);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("You have registered successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email already registered.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to register. Please try again.");
        }
    }

    @PostMapping("/admin/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addAdmin (@Valid @RequestBody Customer nuevoCust) {
        try {
            nuevoCust.setRole(UserType.ADMIN);
            customerService.register(nuevoCust);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("You have registered successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email already registered.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to register. Please try again.");
        }
    }


    @PostMapping("/public/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest logInRequest, HttpServletRequest request){

        String jwt = authService.getJwtFromCookies(request);
        System.out.println(jwt);

        if (jwt != null ){
            return ResponseEntity.badRequest().body("You have to log out first");
        }

        if (authService.authenticate(logInRequest.getEmail(), logInRequest.getPassword())){
            ResponseCookie jwtCookie = authService.generateJwtCookie(logInRequest);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body("Correct authentication");
        }
        return ResponseEntity.badRequest().body("Invalid credentials");
    }


    @PostMapping("/public/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {

        String jwt = authService.getJwtFromCookies(request);
        System.out.println(jwt);

        if (jwt == null || !token.validateToken(jwt)){
             return ResponseEntity.badRequest().body("You have to log in first");

        }
        ResponseCookie cookie = ResponseCookie.from(jwtCookie, "")
                .path("/").maxAge(0).httpOnly(true).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logged out successfully. Cookies cleared.");
    }
}
