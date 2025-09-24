package org.example.steps.specific;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.core.Response;
import org.example.api.data.entity.Card;
import org.example.api.data.request.UpdateRequest;
import org.example.api.data.request.WithdrawRequest;
import org.example.apicalls.apiconfig.BankAPI;
import org.example.apicalls.service.BankService;
import org.example.context.AbstractSteps;
import org.junit.Assert;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Steps de withdraws y actualización de límites.
 *
 * - Reutilizamos la sesión autenticada de AuthenticationSteps.bankService.
 * - Extendemos AbstractSteps para acceder a testContext().
 * - IMPORTANTE: tras cada request guardamos la Response en testContext()
 *   para que los Then de verificación puedan leerla.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class WithdrawSteps extends AbstractSteps {

  public static final BankService bankService = AuthenticationSteps.bankService;

  private BankAPI proxy() {
    return bankService.proxy;
  }

  private Integer currentCardId;
  private Response lastResponse;

  @And("I set the test cardId to my first registered card")
  public void iSetTheTestCardIdToMyFirstRegisteredCard() {
    List<Card> cards = testContext().getCards();
    assertNotNull("No se han registrado tarjetas en el contexto", cards);
    if (cards.isEmpty()) {
      throw new AssertionError("El cliente registrado no tiene tarjetas");
    }
    this.currentCardId = cards.get(0).getCardId();
    System.out.println("Using registered cardId = " + this.currentCardId);
  }

  @And("I create a withdraw of {double} EUR with the current card")
  public void iCreateAWithdrawWithCurrentCard(double amount) {
    assertNotNull("No hay cardId en contexto (usa 'I set the test cardId ...')", currentCardId);
    WithdrawRequest wr = new WithdrawRequest();
    wr.setCardId(currentCardId);
    wr.setAmount(amount);
    lastResponse = proxy().createWithdraw(wr, null);
    testContext().setResponse(lastResponse);
    System.out.println("Create withdraw status: " + lastResponse.getStatus());
  }

  @And("I repeatedly create withdraws summing to {double} EUR with the current card")
  public void iCreateWithdrawsSummingTo(double total) {
    assertNotNull("No hay cardId en contexto (usa 'I set the test cardId ...')", currentCardId);

    double remaining = total;
    while (remaining > 0) {
      double step = Math.min(200.0, remaining);
      WithdrawRequest wr = new WithdrawRequest();
      wr.setCardId(currentCardId);
      wr.setAmount(step);
      lastResponse = proxy().createWithdraw(wr, null);
      testContext().setResponse(lastResponse);
      System.out.println("Create withdraw (" + step + ") status: " + lastResponse.getStatus());
      // Si ya se excede límite, el backend debería devolver 400
      if (lastResponse.getStatus() >= 400) break;
      remaining -= step;
    }
  }

  @When("I update the daily limit to {double} for the current card")
  public void iUpdateDailyLimit(double newLimit) {
    assertNotNull("No hay cardId en contexto (usa 'I set the test cardId ...')", currentCardId);
    UpdateRequest ur = new UpdateRequest();
    ur.setDailyLimit(newLimit);
    lastResponse = proxy().updateDailyLimit(currentCardId, ur, null);
    testContext().setResponse(lastResponse);
    System.out.println("Update daily limit status: " + lastResponse.getStatus());
  }

  @And("I update the monthly limit to {double} for the current card")
  public void iUpdateMonthlyLimit(double newLimit) {
    assertNotNull("No hay cardId en contexto (usa 'I set the test cardId ...')", currentCardId);
    UpdateRequest ur = new UpdateRequest();
    ur.setMonthlyLimit(newLimit);
    lastResponse = proxy().updateMonthlyLimit(currentCardId, ur, null);
    testContext().setResponse(lastResponse);
    System.out.println("Update monthly limit status: " + lastResponse.getStatus());
  }

}
