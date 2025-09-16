package org.example.steps.specific;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.example.api.data.entity.Transfer;
import org.example.api.data.request.TransferRequest;
import org.example.api.service.TransferService;
import org.example.apicalls.apiconfig.BankAPI;
import org.example.apicalls.service.BankService;
import org.example.context.AbstractSteps;
import org.junit.Assert;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferSteps extends AbstractSteps {

    private Response response;
    private BankService bankService = testContext().getBankService();
    private BankAPI proxy = bankService.proxy;

    @When("The customer make a transfer with their main account and transferAmount {double} to an account with id {int}")
    public void theCustomerMakeTransferWithTheirMainAccountAndTransferAmountToAnAccountWithId(Double transferAmount, int receiverAccountId){

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setOriginAccountId(testContext().getOriginID());
        transferRequest.setTransferAmount(transferAmount);
        transferRequest.setCurrencyType(Transfer.CurrencyType.EUR);
        transferRequest.setReceivingAccountId(receiverAccountId);

        Response transferResponse= bankService.doNewTransfer(transferRequest,null);

        testContext().setResponse(transferResponse);
    }
    /*
        @When("i request this users main account sent transfers")
        public void iRequestThisUsersMainAccountSentTransfers() {

        }
        @Then("i should receive the sent transfers")
        public void iShouldReceiveTheSentTransfers() {

        }

        @When("i request this users main account received transfers")
        public void iRequestThisUsersMainAccountReceivedTransfers() {

        }
        @Then("i should receive the received transfers")
        public void iShouldReceiveTheReceivedTransfers() {

        }
    */
    @When("i request all of this users main account transfers")
    public void iRequestAllOfThisUsersMainAccountTransfers() {
        int accountId = 1;
        response = bankService.getTransferHistory(accountId);
        testContext().setResponse(response);
    }
    @Then("i should receive all of the transfers")
    public void iShouldReceiveAllOfTheTransfers() {
        response = testContext().getResponse();
        try {
            assertEquals(200, response.getStatus());
        } catch (AssertionError e) {
            String mensaje = response.readEntity(String.class);
            System.out.println("Test fallido. Código de error: " + response.getStatus());
            System.out.println("Mensaje de error: " + mensaje);
            throw e;
        }

        List<Transfer> transfers = response.readEntity(new GenericType<List<Transfer>>() {});
        for (Transfer t: transfers) {
            System.out.println(t);
        }
    }
}
