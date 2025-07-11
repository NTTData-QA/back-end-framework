package org.example.steps.specific;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.core.Response;
import org.example.api.data.entity.Account;
import org.example.apicalls.apiconfig.BankAPI;
import org.example.apicalls.service.BankService;
import org.example.context.AbstractSteps;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    response =proxy.getUserAmount(null);
    assertEquals(HttpStatus.OK.value(), response.getStatus());
  }

  @Then("i should receive the amount")
  public void iShouldReceiveTheAmount() {
    String amount = response.readEntity(String.class);
    System.out.println("The amount of the logged user is ".concat(amount).concat(" euros"));
  }

  @And("The customer creates {int} account with {double} euros each")
  public void theCustomerCreatesAccountWithEurosEach(int numberOfAccount, double euros) {

    while(numberOfAccount>0) {
      Account account = new Account();
      account.setAmount(euros);
      account.setAccountType(Account.AccountType.BUSINESS_ACCOUNT);
      Response accountResponse = bankService.doNewAccount(account,null);

      assertEquals(201,accountResponse.getStatus());
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
    assertEquals(200,receiverAccountresponse.getStatus());
  }
}
