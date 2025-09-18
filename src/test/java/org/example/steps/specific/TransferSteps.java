package org.example.steps.specific;

import io.cucumber.java.en.And;
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

import static org.junit.jupiter.api.Assertions.*;

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

    @When("i request all of this users transfers with accountId {int}")
    public void iRequestAllOfThisUsersTransfersWithAccountIdAccId(int accountId) {
        response = bankService.getTransferHistory(accountId);
        testContext().setResponse(response);
    }

    @And("i should receive all of the transfers or an error message")
    public void iShouldReceiveAllOfTheTransfersOrAnErrorMessage() {
        response = testContext().getResponse();
        try {
            assertEquals(200, response.getStatus());
            List<Transfer> transfers = response.readEntity(new GenericType<List<Transfer>>() {});
            assertNotNull(transfers);
            assertFalse(transfers.isEmpty());
            for (Transfer t: transfers) {
                System.out.println(t);
            }
        } catch (Error e) {
            String mensaje = response.readEntity(String.class);
            System.out.println("Test fallido. Código de error: " + response.getStatus());
            System.out.println("Mensaje de error: " + mensaje);
            assertNotNull(mensaje);
        }
    }
}
