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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AccountsSteps extends AbstractSteps {

    private final BankService bankService = testContext().getBankService();


    @When("i request this users account information")
    public void iRequestThisUsersAccountInformation() {
        Response response = bankService.getLoggedUserAccounts();
        testContext().setResponse(response);
    }

    @When("i request this users account amount")
    public void iRequestThisUsersAccountAmount() {
        Response response = bankService.getLoggedUserAmount();
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        testContext().setResponse(response);
    }

    @Then("i should receive the amount")
    public void iShouldReceiveTheAmount() {
        Response response = testContext().getResponse();
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
        Response receiverAccountresponse = bankService.getAccountById(receiverAccountId);
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

    @When("i try to delete an another customer's account")
    public void iTryToDeleteAnAccountWithId() {
        int accountId = testContext().getOriginID();
        Response deleteResponse = bankService.doDeleteAccountById(accountId);
        testContext().setResponse(deleteResponse);
    }

    @Given("a customer has an account with id {int}")
    public void aCustomerHasAnAccountWithId(int accountId) {
        Response response = bankService.getAccountById(accountId);
        assertEquals(200, response.getStatus());
        testContext().setOriginID(accountId);
    }
}
