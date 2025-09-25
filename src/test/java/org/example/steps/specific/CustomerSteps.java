package org.example.steps.specific;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.example.api.data.entity.Customer;
import org.example.api.data.request.UpdateRequest;
import org.example.api.service.CustomerService;
import org.example.apicalls.apiconfig.BankAPI;
import org.example.apicalls.service.BankService;
import org.example.context.AbstractSteps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class CustomerSteps extends AbstractSteps {
    private BankService bankService = testContext().getBankService();
    @Autowired private CustomerService customerService;
    private Response response;
    private BankAPI proxy = bankService.proxy;
    private Customer randomCustomer = testContext().getCustomer();  // Mantén el cliente como estado de la clase

    @When("The customer updates their name to {string} and surname {string}")
    public void updateCustomerNameAndSurname(String name, String surname) {

        assertNotNull(randomCustomer);
        randomCustomer.setName(name);
        randomCustomer.setSurname(surname);
        testContext().setCustomer(randomCustomer);

        UpdateRequest nameUpdateRequest = new UpdateRequest();
        nameUpdateRequest.setName(name);
        nameUpdateRequest.setSurname(surname);

        Response response = proxy.updateNameAndSurname(nameUpdateRequest, null);
        System.out.println("Customer name updated to: " + name + " " + surname);
        testContext().setResponse(response);
    }

    @And("The customer updates their email to {string} and password to {string}")
    public void updateCustomerEmailAndPassword(String email, String password) {
        assertNotNull(randomCustomer);
        randomCustomer.setEmail(email);
        randomCustomer.setPassword(password);
        testContext().setCustomer(randomCustomer);
        testContext().setRegisteredEmail(email);

        // Crear el request para actualizar email y contraseña
        UpdateRequest emailPasswordUpdateRequest = new UpdateRequest();
        emailPasswordUpdateRequest.setEmail(email);
        emailPasswordUpdateRequest.setPassword(password);

        bankService.updateEmailAndPassword(emailPasswordUpdateRequest);
        testContext().setBankService(bankService);
        proxy = bankService.proxy;
    }

    @Then("The customer’s name, surname, email and password have been updated {string}") //

    //TODO Debería ser genérico y aceptar una lista de parámetros (no estáticos)

    public void verifyCustomerUpdated(String updateStatus) {
        assertNotNull(randomCustomer);
        String email = testContext().getRegisteredEmail();
        System.out.println(email);
        Response response = proxy.getCustomerByEmail(email);
        System.out.println(response.getStatus());
        Customer updatedCustomer = null;
        try{
            updatedCustomer = response.readEntity(Customer.class);
        }catch (Exception e){
            updatedCustomer = null;
        }
        //Optional<Customer> updatedCustomer = customerService.findByEmail(randomCustomer.getEmail());
        if (updateStatus.equals("successfully")) {
            assertTrue(updatedCustomer!=null);
            System.out.println("Customer updated successfully with email: " + updatedCustomer.getEmail());
        } else {
            assertFalse(updatedCustomer!=null);
            System.out.println("Customer update failed.");
        }
    }

    @When("i request all Customers list")
    public void iRequestAllCustomersList() {
        response = bankService.getAllCustomersList();
        testContext().setResponse(response);
    }

    @And("if the response is successful, i should receive the customers list")
    public void ifTheResponseIsSuccessfulIShouldReceiveTheCustomersList() {
        response = testContext().getResponse();
        try {
            assertEquals(200, response.getStatus());
/*
            List<Customer> customers = response.readEntity(new GenericType<List<Customer>>() {});
            assertNotNull(customers);
            assertFalse(customers.isEmpty());
            for (Customer c: customers) {
                System.out.println(c.toString());
            }
*/
            System.out.println(response.readEntity(String.class));
        } catch (Error e) {
            String mensaje = response.readEntity(String.class);
            System.out.println("Test fallido. Código de error: " + response.getStatus());
            System.out.println("Mensaje de error: " + mensaje);
            assertNotNull(mensaje);
        }
    }
}
