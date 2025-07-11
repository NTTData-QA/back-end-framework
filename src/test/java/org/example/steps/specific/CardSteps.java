package org.example.steps.specific;

import io.cucumber.java.en.And;
import jakarta.ws.rs.core.Response;
import org.example.api.data.entity.Card;
import org.example.apicalls.apiconfig.BankAPI;
import org.example.apicalls.service.BankService;
import org.example.apicalls.utils.JsonConverter;
import org.example.context.AbstractSteps;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        assertEquals(jsonOutput,readResponse);

        testContext().setResponse(response);
    }
}
