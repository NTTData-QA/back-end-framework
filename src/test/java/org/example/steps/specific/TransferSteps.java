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

    private BankService bankService = testContext().getBankService();

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
}
