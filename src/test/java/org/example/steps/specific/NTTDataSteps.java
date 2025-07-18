package org.example.steps.specific;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.core.Response;
import org.example.apicalls.client.PageClient;
import org.example.context.AbstractSteps;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class NTTDataSteps extends AbstractSteps {

    private Response response = null;

    @When("the user navigates to {string}")
    public void theUserNavigatesTo(String url) throws NoSuchAlgorithmException, KeyManagementException {
        PageClient nttDatClient = new PageClient();
        response = nttDatClient.getPageResponse(url);
    }

    @Then("the status code is {int}")
    public void theStatusCodeIs(int statusCode) {
        assertEquals(statusCode, response.getStatus(), String.format("NTTData landing page status is not %d", statusCode));
    }
}
