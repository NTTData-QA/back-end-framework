package org.example.steps.specific;

import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import jakarta.ws.rs.core.Response;
import org.example.api.data.entity.Card;
import org.example.apicalls.service.BankService;
import org.example.apicalls.utils.JsonConverter;
import org.example.context.AbstractSteps;
import org.junit.Assert;

import java.util.List;

public class CardSteps extends AbstractSteps {

    private final BankService bankService = testContext().getBankService();


    @And("The customer checks their cards")
    public void theCustomerChecksTheirCards(){
        Response response = bankService.doGetLoggedUserCards();
        List<Card> cards = testContext().getCards();

        JsonConverter jsonConverter = new JsonConverter();
        String jsonOutput = jsonConverter.convertListToJson(cards);
        String readResponse = response.readEntity(String.class);

        Assert.assertEquals(jsonOutput,readResponse);

        testContext().setResponse(response);
    }
    @When("I create a card to the account: {int}")
    public void iCreateaCardToTheAccount(int accountId){
        Response response = bankService.doNewCard(accountId, Card.CardType.DEBIT);
        testContext().setResponse(response);
    }

}
