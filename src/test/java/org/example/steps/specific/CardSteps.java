package org.example.steps.specific;

import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import org.example.api.data.entity.Card;
import org.example.api.data.request.CardRequest;
import org.example.apicalls.apiconfig.BankAPI;
import org.example.apicalls.service.BankService;
import org.example.apicalls.utils.JsonConverter;
import org.example.context.AbstractSteps;
import org.junit.Assert;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public class CardSteps extends AbstractSteps {
    private Response response;
    private BankService bankService = testContext().getBankService();
    private BankAPI proxy = bankService.proxy;

    @And("The customer checks their cards")
    public void theCustomerChecksTheirCards(){
        response = proxy.getCards();
        List<Card> cards = testContext().getCards();

        JsonConverter jsonConverter = new JsonConverter();
        String jsonOutput = jsonConverter.convertListToJson(cards);
        String readResponse = response.readEntity(String.class);

        Assert.assertEquals(jsonOutput,readResponse);

        testContext().setResponse(response);
    }
    @When("I create a card to the account: {int}")
    public void iCreateaCardToTheAccount(int accountId){
        CardRequest cardRequest = new CardRequest();
        cardRequest.setType("Debit");
        cardRequest.setAccountId(accountId);
        response = proxy.newCard(cardRequest);
        testContext().setResponse(response);

    }

}
