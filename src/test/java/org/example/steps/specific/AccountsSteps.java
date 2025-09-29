package org.example.steps.specific;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.core.Response;
import org.example.apicalls.apiconfig.BankAPI;
import org.example.apicalls.service.BankService;
import org.example.context.AbstractSteps;
import org.example.steps.utils.StepUtils;
import org.junit.Assert;
import org.opentest4j.AssertionFailedError;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AccountsSteps extends AbstractSteps {
    private Response response;
    private static String jwt;
    private BankService bankService = testContext().getBankService();
    private BankAPI proxy = bankService.proxy;


    @When("i request this users account information")
    public void iRequestThisUsersAccountInformation() {
        response = proxy.getUserAccounts(null);
        testContext().setResponse(response);
    }

    @When("i request this users account amount")
    public void iRequestThisUsersAccountAmount() {
        response = proxy.getUserAmount(null);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Then("i should receive the amount")
    public void iShouldReceiveTheAmount() {
        String amount = response.readEntity(String.class);
        System.out.println("The amount of the logged user is ".concat(amount).concat(" euros"));
    }

    @And("The customer creates {int} account with {double} euros each")
    public void theCustomerCreatesAccountWithEurosEach(int numberOfAccount, double euros) {
        while (numberOfAccount > 0) {
            StepUtils.createAccount(bankService, testContext(), euros);
            numberOfAccount--;
        }
    }

    @And("The receiving customer has an account with id {int}")
    public void theReceivingCustomerHasAnAccountWithId(int receiverAccountId) {
        proxy = bankService.proxy;
        Response receiverAccountresponse = proxy.accountById(receiverAccountId);
        Assert.assertEquals(200, receiverAccountresponse.getStatus());
    }

    @Given("the customer creates an account with {double} euros")
    public void theCustomerCreatesAnAccountWithEuros(double euros) {
        StepUtils.createAccount(bankService, testContext(), euros);
    }

    @And("the customer blocks the account")
    public void theCustomerBlocksTheAccount() {
        Integer accountId = testContext().getOriginID();
        Response blockedResponse = bankService.doUpdateBlockStatus(accountId, true);
        testContext().setResponse(blockedResponse);
    }

    @When("the customer tries to delete the account")
    public void theCustomerTriesToDeleteTheAccount() {
        Integer accountId = testContext().getOriginID();
        Response deleteResponse = bankService.doDeleteAccountById(accountId);
        testContext().setResponse(deleteResponse);
    }

    @Then("the customer should receive the code {int} and a message")
    public void theCustomerShouldReceiveTheCodeAndAMessage(int codigo) {
        response = testContext().getResponse();
        Assert.assertEquals(codigo, response.getStatus());
    }

    @When("i try to delete an another customer's account")
    public void iTryToDeleteAnAccountWithId() {
        int accountId = testContext().getOriginID();
        Response deleteResponse = bankService.doDeleteAccountById(accountId);
        testContext().setResponse(deleteResponse);
    }

    @Then("i should receive the code {int} and a status message")
    public void iShouldReceiveTheCodeCodeAndAStatusMessage(Integer code) {
        response = testContext().getResponse();
        String mensaje = response.readEntity(String.class);
        try {
            assertEquals(code, response.getStatus());
            System.out.println("Resultado correcto. Código: " + response.getStatus());
            System.out.println("Mensaje: " + mensaje);
            assertNotNull(mensaje);
        } catch (Error e) {
            System.out.println("Test fallido. Código de error: " + response.getStatus());
            System.out.println("Mensaje de error: " + mensaje);
            assertNotNull(mensaje);
            throw new AssertionFailedError();
        }
    }
}
