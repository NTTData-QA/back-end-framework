package org.testRestEasyClient;

import org.example.api.Application;
import org.example.apicalls.apiconfig.BankAPI;
import org.example.apicalls.client.BankClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OpenBankTest {

    private BankClient bankClient = new BankClient();

    private BankAPI proxy = null;

    private final String landingPageURL = "https://www.openbank.es/";

    @BeforeAll
    public static void runSpringBoot(){
        Application.main(new String[]{});
    }

    @BeforeEach
    public void setup() {
        proxy = bankClient.getAPI(landingPageURL);
    }

    @Test
    @DisplayName("Validate landing page status code 200")
    public void landingPageTest() {
        assertEquals(200, proxy.getPage().getStatus(), " Open Bank landing page status is not 200");
    }
}
