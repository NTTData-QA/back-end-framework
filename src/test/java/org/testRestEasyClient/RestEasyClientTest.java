package org.testRestEasyClient;

import jakarta.ws.rs.core.NewCookie;
import org.example.api.Application;
import org.example.api.data.request.LoginRequest;
import org.example.apicalls.apiconfig.BankAPI;
import jakarta.ws.rs.core.Response;
import org.example.apicalls.client.BankClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class RestEasyClientTest {

    @Autowired
    private BankClient bankClient = new BankClient() ;

    private Integer id = 1;
    private Response loginResponse;
    private String jsonLoginRequest;

    BankAPI proxy = null;
    LoginRequest loginRequest = new LoginRequest();
    @BeforeAll
    public static void runSpringBoot(){
        Application.main(new String[]{});
    }

    @BeforeEach
    public void setup(){
        proxy = bankClient.getAPI();

        //jsonLoginRequest = "{ \"email\": \"john.doe@example.com\", \"password\": \"password123\" }";;

        loginRequest.setEmail("john.doe@example.com");
        loginRequest.setPassword("password123");

        loginResponse = proxy.login(loginRequest, null);
        System.out.println("HTTP Status First Login: "+ loginResponse.getStatus());
        System.out.println("Codigo login");
        /*
        // Idem que BankService. Con el CookieManager se gestionan todas las cookies automáticamente
        Map<String, NewCookie> cookies = loginResponse.getCookies();
        NewCookie newCookie = cookies.entrySet().iterator().next().getValue();
        proxy = bankClient.getAPI(newCookie);
        */
    }

    @Test
    public void testGetAccountById() {
        Response accountResponse = proxy.accountById(id);

        System.out.println("HTTP Status accountById: "+ accountResponse.getStatus() );
        System.out.println(accountResponse.readEntity(String.class));


    }

    @Test
    public void testSecondLogin(){
        System.out.println(loginRequest);
        Response loginResponse2 = proxy.login(loginRequest, null);
        System.out.println("HTTP Status Second Login: "+ loginResponse2.getStatus());
        System.out.println("Mensaje recibido:" + loginResponse2.readEntity(String.class));
    }
}

