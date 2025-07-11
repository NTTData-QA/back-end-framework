package org.example.steps.specific;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.apicalls.apiconfig.BankAPI;
import org.example.apicalls.client.BankClient;
import org.example.context.AbstractSteps;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NTTDataSteps extends AbstractSteps {

    private BankAPI proxy = null;

    @When("the user navigates to {string}")
    public void theUserNavigatesTo(String url) {
        BankClient bankClient = new BankClient();
        proxy = bankClient.getAPI(url);
    }

    @Then("the status code is {int}")
    public void theStatusCodeIs(int statusCode) {
        assertEquals(statusCode, proxy.getPage().getStatus(), String.format("NTTData landing page status is not %d", statusCode));
    }
}
