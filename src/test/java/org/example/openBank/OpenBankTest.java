package org.example.openBank;

import jakarta.ws.rs.core.Response;
import org.example.apicalls.client.PageClient;
import org.example.util.JunitReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(JunitReporter.class)
@SpringBootTest
public class OpenBankTest {

    @Autowired
    private PageClient bankClient;

    private Response response = null;

    @BeforeEach
    public void setup() throws NoSuchAlgorithmException, KeyManagementException {
        String landingPageURL = "https://www.openbank.es/";
        response = bankClient.getPageResponse(landingPageURL);
    }

    @Test
    @DisplayName("Validate landing page status code 200")
    public void landingPageTest() {
        assertEquals(200, response.getStatus(), " Open Bank landing page status is not 200");
    }
}
