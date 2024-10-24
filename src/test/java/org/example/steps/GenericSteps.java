package org.example.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import jakarta.ws.rs.core.Response;
import org.example.api.data.entity.Account;
import org.example.api.data.entity.Card;
import org.example.api.data.entity.Customer;
import org.example.api.data.repository.AccountRepository;
import org.example.api.data.repository.CardRepository;
import org.example.api.data.repository.CustomerRepository;
import org.example.api.data.request.CardRequest;
import org.example.apicalls.service.BankService;
import org.example.apicalls.utils.Generator;
import org.example.apicalls.utils.JsonConverter;
import org.example.context.AbstractSteps;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class GenericSteps extends AbstractSteps {
    private Response response;
    private BankService bankService = new BankService();
    @Autowired private CustomerRepository customerRepository;
    @Then("The customer gets a {int} status response and message: {string}")
    public void theCustomerGetsStatusResponseAndBody(Integer expectedStatus, String expectedMessage){
        //Se recibe la respuesta y se extrae el mensaje y el status de la response
        response = testContext().getResponse();
        Integer receivedStatus = response.getStatus();
        String receivedMessage= response.readEntity(String.class);

        // Comprobamos que el status y el mensaje de la response sean los esperados
        Assert.assertEquals(expectedStatus,receivedStatus);
        Assert.assertEquals(expectedMessage,receivedMessage);

        // Reseteamos el contexto (Esto deberia añadirse en un @AfterEach para asegurarse que los test vayan bien)
        testContext().reset();
    }

    @Then("The customer gets a {int} status response")
    public void theCustomerGetsAStatusStatusResponse(Integer expectedStatus) {
        //Se recibe la respuesta y se extrae el mensaje y el status de la response
        response = testContext().getResponse();
        Integer receivedStatus = response.getStatus();

        // Comprobamos que el status y el mensaje de la response sean los esperados
        Assert.assertEquals(expectedStatus,receivedStatus);

        // Reseteamos el contexto (Esto deberia añadirse en un @AfterEach para asegurarse que los test vayan bien)
        testContext().reset();
    }

    @Given("The customer registers with {int} accounts, {int} cards and an initial amount of {double}")
    public void theCustomerRegistersWithAccountsCardsAndInitialAmount(int nAccounts, int ncards, double amount){
        Customer randcustomer = Generator.generateRandomCustomer(ncards, nAccounts, amount);
        // register customer
        Customer customer = customerRepository.save(randcustomer);
        testContext().setCustomer(customer);
        testContext().setRegisteredEmail(customer.getEmail());
        // Get accountId from registered receiver customer: receiverAccountId
        Integer receiverAccountId = customer.getAccounts().get(0).getAccountId();

        List<Account> accounts = customer.getAccounts();
        List<Card> cards = new ArrayList<>();
        for(Account account : accounts){
            cards.addAll(account.getCards());
        }

        testContext().setCards(cards);
    }
}
