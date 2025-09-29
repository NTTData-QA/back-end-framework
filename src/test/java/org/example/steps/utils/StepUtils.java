package org.example.steps.utils;

import jakarta.ws.rs.core.Response;
import org.example.api.data.entity.Account;
import org.example.apicalls.service.BankService;
import org.example.context.TestContext;
import org.junit.Assert;

// This class is design to contain generic code snippets to be reused in actual steps
public class StepUtils {

    public static void doLogin (BankService bankService, TestContext testContext, String email, String password) {
        Response response = bankService.doLogin(email,password);
        testContext.setResponse(response);
        testContext.setBankService(bankService);
    }

    public static void createAccount(BankService bankService, TestContext testContext, Double amount) {
        Account account = new Account();
        account.setAmount(amount);
        account.setAccountType(Account.AccountType.BUSINESS_ACCOUNT);

        Response accountResponse = bankService.doNewAccount(account, null);
        Assert.assertEquals(201, accountResponse.getStatus());

        String accountOrigin = accountResponse.readEntity(String.class);
        String[] parts = accountOrigin.split(": ");
        // Extraer el número como String y luego convertirlo a un número entero
        String accountIdString = parts[1];
        int accountId = Integer.parseInt(accountIdString);
        testContext.setOriginID(accountId);
    }

    public static void doLogout(BankService bankService, TestContext testContext) {
        Response response = bankService.doLogout();
        testContext.setResponse(response);
    }
}
