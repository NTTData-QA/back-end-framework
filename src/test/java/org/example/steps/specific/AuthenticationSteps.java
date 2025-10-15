package org.example.steps.specific;

import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import jakarta.ws.rs.core.Response;
import org.example.api.data.entity.Customer;
import org.example.apicalls.service.BankService;
import org.example.context.AbstractSteps;
import org.example.steps.utils.StepUtils;
import org.junit.Assert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AuthenticationSteps extends AbstractSteps {

    private final BankService bankService = new BankService();


    @Given("the system is ready for user authentication")
    public void systemIsReady() {
        RestAssured.baseURI = "http://localhost:8080";
    }

    @Given("The customer registers with random email, name, surname and password")
    public void registerRandomCustomer() {
        Customer randomCustomer = bankService.registerRandomCustomer();  // Almacena el cliente generado
        System.out.println("Customer registered: " + randomCustomer.getEmail());
        assertNotNull(randomCustomer.getEmail());
        testContext().setCustomer(randomCustomer);
        testContext().setRegisteredEmail(randomCustomer.getEmail());
    }


    @When("I register with name {string}, surname {string}, email {string} and password {string}")
    public void registerUser(String name, String surname, String email, String password) {
        StepUtils.doRegister(bankService, testContext(), name, surname, email, password);
    }


    @Given("I have registered with name {string}, surname {string}, email {string} and password {string}")
    public void registerForLogin(String name, String surname, String email, String password) {
        StepUtils.doRegister(bankService, testContext(), name, surname, email, password);
    }

    @Given("the system is ready and i log with email {string} and password {string}")
    public void theSystemIsReadyAndILogWithEmailAndPassword(String email, String password) {
        StepUtils.doLogin(bankService, testContext(), email, password);
    }

    @When("I login with email {string} and password {string}")
    public void loginUser(String email, String password) {
        StepUtils.doLogin(bankService, testContext(), email, password);
    }

    @Given("I have logged in with email {string} and password {string}")
    public void loginUserForLogout(String email, String password) {
        StepUtils.doLogin(bankService, testContext(), email, password);
    }


    @When("The customer logins with  email {string} and  password {string}")
    public void theCustomerLoginsWithEmailAndMyPassword(String email,String password){
        StepUtils.doLogin(bankService, testContext(), email, password);
    }

    @And("The customer logging with the register credentials")
    public void theCustomerLogginWithTheRegisterCredentials(){
        Customer randomCustomer = testContext().getCustomer();
        String email = randomCustomer.getEmail();
        String password = randomCustomer.getPassword();
        StepUtils.doLogin(bankService, testContext(), email, password);
        assertEquals(200,testContext().getResponse().getStatus());
    }


    @When("The customer logs in with their register credentials")
    public void theCustomerLogsInWithTheirRegisterCredentials() {
        String email = testContext().getCustomer().getEmail();
        String password = testContext().getCustomer().getPassword();
        StepUtils.doLogin(bankService, testContext(), email, password);
        Assert.assertEquals(200,testContext().getResponse().getStatus());
    }

    @When("I log out")
    public void logoutUser() {
        StepUtils.doLogout(bankService, testContext());
    }

//    @After
//    public void deleteRegisteredUser() {
//    registeredEmail = testContext().getRegisteredEmail();
//    proxy = bankService.proxy;
//    testContext().reset();
//    System.out.println(registeredEmail);
//        if (registeredEmail != null) {
//            response = proxy.deleteLoggedUserCards(null);
//            System.out.println("Status de borrar cards: "+response.getStatus());
//            response = proxy.deleteLoggedUserAccounts(null);
//            System.out.println("Status de borrar cuentas: "+response.getStatus());
//
//            Response deleteResponse = proxy.deleteCustomer(registeredEmail);
//            int statusCode = deleteResponse.getStatus();
//            System.out.println("Delete response status code: " + statusCode);
//            Assert.assertEquals(HttpStatus.OK.value(), statusCode);  // Validar si realmente devolvió un 200 OK
//        } else {
//            System.out.println("No user to delete, registeredEmail is null");
//        }
//    }

    @After()
    public void deleteRegisteredUser() {
        String registeredEmail = testContext().getRegisteredEmail();

        // If there's no registered email in the context, there's nothing to clean up.
        if (registeredEmail == null) {
            System.out.println("No registered email in test context, skipping cleanup.");
            testContext().reset();
            return;
        }

        // Log out any active session to ensure a clean state.
        StepUtils.doLogout(bankService, testContext());

        // Log in as admin to get deletion permissions.
        StepUtils.doLogin(bankService, testContext(), "admin@admin.com", "1234");

        Response customerResponse = bankService.getCustomerByEmail(registeredEmail);

        if (customerResponse.getStatus() == HttpStatus.OK.value()) {
            Customer customerToDelete = customerResponse.readEntity(Customer.class);
            Response response = bankService.doDeleteCustomerById(customerToDelete.getCustomerId());
            Assert.assertEquals(200, response.getStatus());
            System.out.println("Successfully deleted user: " + registeredEmail);
        } else {
            System.out.println("Could not find user by email '" + registeredEmail + "' to delete. Status: " + customerResponse.getStatus());
        }

        // Clean up the test context for the next run.
        testContext().reset();
    }

    @When("The customer deletes his customer registration by id {int}")
    public void theCustomerDeleteHisCustomerRegistrationById(int customerId) {
        Response deleteResponse = bankService.doDeleteCustomerById(customerId);
        testContext().setResponse(deleteResponse);
        int statusCode = deleteResponse.getStatus();
        Assert.assertEquals(HttpStatus.OK.value(), statusCode);  // Validar si realmente devolvió un 200 OK

        StepUtils.doLogout(bankService, testContext());

        String registeredEmail = testContext().getRegisteredEmail();
        System.out.println(registeredEmail != null ? registeredEmail : "registeredEmail is null");

        if (registeredEmail != null) {
            // Borrar withdraws de las tarjetas registradas
            var cards = testContext().getCards();
            if (cards != null) {
                for (var card : cards) {
                    if (card != null && card.getCardId() != null) {
                        Response deleteWithdrawsResponse = bankService.doDeleteWithdrawsByCardId(card.getCardId());
                        System.out.println("Delete withdraws for card " + card.getCardId() + " -> " + deleteWithdrawsResponse.getStatus());
                    }
                }
            }

            // Ejecuta y loguea cualquier borrado, validando que sea 200
            Response deleteCardsResponse = bankService.doDeleteLoggedUserCards();
            System.out.println("Delete cards -> " + deleteCardsResponse.getStatus());
            assertEquals(200, deleteCardsResponse.getStatus());

            Response deleteLoggedUserResponse = bankService.doDeleteLoggedUserAccounts();
            System.out.println("Delete accounts -> " + deleteLoggedUserResponse.getStatus());
            assertEquals(200, deleteLoggedUserResponse.getStatus());

            Response deleteCustomerResponse = bankService.doDeleteCustomerByEmail(registeredEmail);
            System.out.println("Delete customer -> " + deleteCustomerResponse.getStatus());
            assertEquals(200, deleteCustomerResponse.getStatus());
        } else {
            System.out.println("No user to delete, registeredEmail is null");
        }
        testContext().reset();
    }

    @When("The customer deletes his customer registration by id")
    public void theCustomerDeleteHisCustomerRegistrationById() {
        Integer customerId = bankService.getCustomerByEmail("paula@example.com").readEntity(Customer.class).getCustomerId();
        if (customerId != null) {
            Response deleteResponse = bankService.doDeleteCustomerById(customerId);
            testContext().setResponse(deleteResponse);
            int statusCode = deleteResponse.getStatus();
            Assert.assertEquals(HttpStatus.OK.value(), statusCode);  // Validar si realmente devolvió un 200 OK
        } else {
            System.out.println("No user to delete, customerId is null");
        }
    }
}
