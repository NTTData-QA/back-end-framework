package org.example.steps.specific;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.example.api.data.entity.Customer;
import org.example.apicalls.service.BankService;
import org.example.context.AbstractSteps;
import org.junit.Assert;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class CustomerSteps extends AbstractSteps {

    private final BankService bankService = testContext().getBankService();


    @When("The customer updates their name to {string} and surname {string}")
    public void updateCustomerNameAndSurname(String name, String surname) {
        Response response = bankService.doUpdateNameAndSurname(name, surname);
        System.out.println("Customer name updated to: " + name + " " + surname);
        testContext().setCustomer(bankService.getLoggedCustomer().readEntity(Customer.class));
        testContext().setResponse(response);
    }

    @And("The customer updates their email to {string} and password to {string}")
    public void updateCustomerEmailAndPassword(String email, String password) {
        Response response = bankService.updateEmailAndPassword(email, password);
        testContext().setBankService(bankService);
        testContext().setCustomer(bankService.getLoggedCustomer().readEntity(Customer.class));
        testContext().setResponse(response);
    }

    @Then("The customer’s name, surname, email and password have been updated {string}") //
    //TODO Debería ser genérico y aceptar una lista de parámetros (no estáticos)
    public void verifyCustomerUpdated(String updateStatus) {
        Response response = bankService.getLoggedCustomer();
        Customer updatedCustomer;
        try {
            updatedCustomer = response.readEntity(Customer.class);
        } catch (Exception ignored) {
            updatedCustomer = null;
        }
        //Optional<Customer> updatedCustomer = customerService.findByEmail(randomCustomer.getEmail());
        if (updateStatus.equals("successfully")) {
            Assert.assertNotNull(updatedCustomer);
            System.out.println("Customer updated successfully with email: " + updatedCustomer.getEmail());
        } else {
            Assert.assertNull(updatedCustomer);
            System.out.println("Customer update failed.");
        }
    }

    @When("i request all Customers list")
    public void iRequestAllCustomersList() {
        Response response = bankService.getAllCustomersList();
        testContext().setResponse(response);
    }

    @And("if the response is successful, i should receive the customers list")
    public void ifTheResponseIsSuccessfulIShouldReceiveTheCustomersList() {
        Response response = testContext().getResponse();
        try {
            Assert.assertEquals(200, response.getStatus());
            List<Customer> customers = response.readEntity(new GenericType<>() {});
            Assert.assertNotNull(customers);
            Assert.assertFalse(customers.isEmpty());
            for (Customer c: customers) {
                System.out.println(c.toString());
            }
        } catch (Error e) {
            String mensaje = response.readEntity(String.class);
            System.out.println("Test fallido. Código de error: " + response.getStatus());
            System.out.println("Mensaje de error: " + mensaje);
            Assert.assertNotNull(mensaje);
        }
    }
}
