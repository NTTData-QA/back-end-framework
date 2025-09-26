package org.example.steps.utils;

import jakarta.ws.rs.core.Response;
import org.example.apicalls.service.BankService;
import org.example.context.TestContext;

// This class is design to contain generic code snippets to be reused in actual steps
public class StepUtils {

    public static void doLogin (BankService bankService, TestContext testContext, String email, String password) {
        Response response = bankService.doLogin(email,password);
        testContext.setResponse(response);
        testContext.setBankService(bankService);
    }

}
