package org.example.steps.specific;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.example.api.data.entity.Account;
import org.example.api.data.entity.Customer;
import org.example.apicalls.apiconfig.BankAPI;
import org.example.apicalls.service.BankService;
import org.example.context.AbstractSteps;
import org.junit.Assert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.List;

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
            Account account = new Account();
            account.setAmount(euros);
            account.setAccountType(Account.AccountType.BUSINESS_ACCOUNT);
            Response accountResponse = bankService.doNewAccount(account, null);

            Assert.assertEquals(201, accountResponse.getStatus());
            numberOfAccount--;
            String accountOrigin = accountResponse.readEntity(String.class);
            String[] parts = accountOrigin.split(": ");

            // Extraer el número como String y luego convertirlo a un número entero
            String accountIdString = parts[1];
            int accountId = Integer.parseInt(accountIdString);
            testContext().setOriginID(accountId);
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

        Account account = new Account();
        account.setAmount(euros);
        account.setAccountType(Account.AccountType.BUSINESS_ACCOUNT);

        Response accountResponse = bankService.doNewAccount(account, null);
        Assert.assertEquals(201, accountResponse.getStatus());

        String accountOrigin = accountResponse.readEntity(String.class);
        String[] parts = accountOrigin.split(": ");
        String accountIdString = parts[1];
        int accountId = Integer.parseInt(accountIdString);
        testContext().setOriginID(accountId);
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
    public void theCusromerShouldReceiveTheCodeAndAMessage(int codigo) {
        response = testContext().getResponse();
        Assert.assertEquals(codigo, response.getStatus());
    }

    @When("i try to delete an account with id {int}")
    public void iTryToDeleteAnAccountWithId(int accountId) {
        Response deleteResponse = bankService.doDeleteAccountById(accountId);
        testContext().setResponse(deleteResponse);
    }

    @Then("i should receive the code {int} and a status message")
    public void iShouldReceiveTheCodeCodeAndAStatusMessage(Integer code) {
        response = testContext().getResponse();
        assertEquals(code, response.getStatus());
        String mensaje = response.readEntity(String.class);
        System.out.println("Código: " + response.getStatus());
        System.out.println("Mensaje: " + mensaje);
        assertNotNull(mensaje);
    }
}
