package org.example.steps.specific;

import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import jakarta.ws.rs.core.Response;
import org.example.api.data.entity.Customer;
import org.example.apicalls.apiconfig.BankAPI;
import org.example.apicalls.service.BankService;
import org.example.context.AbstractSteps;
import org.junit.Assert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AuthenticationSteps extends AbstractSteps {

    private Response response;
    public static BankService bankService = new BankService();
    private  String registeredEmail = testContext().getRegisteredEmail();
    private final String baseUrl = "http://localhost:8080";
    private static BankAPI proxy = bankService.proxy;


    @Given("the system is ready for user authentication")
    public void systemIsReady() {
        RestAssured.baseURI = baseUrl;
    }

    @Given("The customer registers with random email, name, surname and password")
    public void registerRandomCustomer() {
        Customer randomCustomer = bankService.registerRandomCustomer();  // Almacena el cliente generado
        System.out.println("Customer registered: " + randomCustomer.getEmail());
        assertNotNull(randomCustomer.getEmail());
        testContext().setCustomer(randomCustomer);
        testContext().setRegisteredEmail(randomCustomer.getEmail());
    }


    @When("I register with name {string}, surname {string}, email {string} and password {string} and I log in")
    public void registerUser(String name, String surname, String email, String password) {
        testContext().setRegisteredEmail(email);
        response = bankService.doRegister(name, surname, email, password);
        Customer customer = bankService.registerCustomer(name, surname, email, password);
        testContext().setCustomer(customer);
        testContext().setResponse(response);
        bankService.doLogin(email,password);
        testContext().setBankService(bankService);
    }


    @Given("I have registered with name {string}, surname {string}, email {string} and password {string}")
    public void registerForLogin(String name, String surname, String email, String password) {
        registeredEmail = email;
        response = bankService.doRegister(name,surname,email,password);
        testContext().setRegisteredEmail(email);
    }

    @Given("the system is ready and i log with email {string} and password {string}")
    public void theSystemIsReadyAndILogWithEmailAndPassword(String email, String password) {
        response = bankService.doLogin(email,password);
        testContext().setResponse(response);
        testContext().setBankService(bankService);
    }

    @When("I login with email {string} and password {string}")
    public void loginUser(String email, String password) {
        response = bankService.doLogin(email,password);
        testContext().setResponse(response);
        testContext().setBankService(bankService);
    }

    @Given("I have logged in with email {string} and password {string}")
    public void loginUserForLogout(String email, String password) {
        response = bankService.doLogin(email,password);
        System.out.println(response.readEntity(String.class));
        testContext().setResponse(response);
        testContext().setBankService(bankService);
    }


    @When("The customer logins with  email {string} and  password {string}")
    public void theCustomerLoginsWithEmailAndMyPassword(String email,String password){
        response = bankService.doLogin(email,password);
        testContext().setResponse(response);
        testContext().setBankService(bankService);
    }

    @And("The customer logging with the register credentials")
    public void theCustomerLogginWithTheRegisterCredentials(){
        Customer randomCustomer = testContext().getCustomer();
        String email = randomCustomer.getEmail();
        String password = randomCustomer.getPassword();
        Response response = bankService.doLogin(email,password);
        testContext().setBankService(bankService);
        assertEquals(200,response.getStatus());
    }


    @When("The customer logs in with their register credentials")
    public void theCustomerLogsInWithTheirRegisterCredentials() {
        String email = testContext().getCustomer().getEmail();
        String password = testContext().getCustomer().getPassword();
        response = bankService.doLogin(email,password);
        testContext().setBankService(bankService);
        Assert.assertEquals(200,response.getStatus());
    }

    @When("I log out")
    public void logoutUser() {
        response = bankService.doLogout();
        testContext().setResponse(response);
    }

//    @After
//    public void deleteRegisteredUser() {
//    registeredEmail = testContext().getRegisteredEmail();
//    proxy = bankService.proxy;
//    testContext().reset();
//    System.out.println(registeredEmail);
//        if (registeredEmail != null) {
//            response = proxy.deleteCardsOfLoggedUser(null);
//            System.out.println("Status de borrar cards: "+response.getStatus());
//            response = proxy.deleteLoggedUser(null);
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

    @After
    public void deleteRegisteredUser() {

        // NO resetear el contexto todavía: necesitamos email y cards
        String registeredEmail = testContext().getRegisteredEmail();
        proxy = bankService.proxy; // usar el proxy autenticado actual

        System.out.println(registeredEmail != null ? registeredEmail : "registeredEmail is null");

        if (registeredEmail != null) {
            // 1) Borrar withdraws de cada tarjeta conocida en el contexto (si existen)
            try {
                var cards = testContext().getCards(); // las guardaste en el Given de registro
                if (cards != null && !cards.isEmpty()) {
                    for (var card : cards) {
                        if (card != null && card.getCardId() != null) {
                            Response r = proxy.deleteWithdrawsById(card.getCardId());
                            System.out.println("Delete withdraws for card " + card.getCardId() + " -> " + r.getStatus());
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("WARN deleting withdraws by context cards: " + e.getMessage());
            }

            // 3) Borrar todas las tarjetas del usuario logueado
            try {
                Response resp = proxy.deleteCardsOfLoggedUser(null);
                System.out.println("Delete cards of logged user -> " + resp.getStatus());
                // Acepta 200/204/404 para que el teardown no rompa
                Assert.assertTrue(resp.getStatus() == 200 || resp.getStatus() == 204 || resp.getStatus() == 404);
            } catch (Exception e) {
                System.out.println("WARN deleting cards: " + e.getMessage());
            }

            // 4) Borrar cuentas del usuario logueado
            try {
                Response resp = proxy.deleteLoggedUser(null);
                System.out.println("Delete accounts of logged user -> " + resp.getStatus());
                Assert.assertTrue(resp.getStatus() == 200 || resp.getStatus() == 204 || resp.getStatus() == 404);
            } catch (Exception e) {
                System.out.println("WARN deleting accounts: " + e.getMessage());
            }

            // 5) Borrar el propio Customer por email
            Response deleteResponse = proxy.deleteCustomer(registeredEmail);
            int statusCode = deleteResponse.getStatus();
            System.out.println("Delete customer status code: " + statusCode);
            Assert.assertTrue("Unexpected status deleting customer: " + statusCode,
                    statusCode == 200 || statusCode == 204 || statusCode == 404);

        } else {
            System.out.println("No user to delete, registeredEmail is null");
        }

        // 6) AHORA sí: limpiar el contexto de test
        testContext().reset();
    }

    @When("The customer deletes his customer registration by id {int}")
    public void theCustomerDeleteHisCustomerRegistrationById(int customerId) {
        proxy = bankService.proxy;
        if (Integer.valueOf(customerId) != null) {
            Response deleteResponse = proxy.deleteCustomerById(customerId);
            testContext().setResponse(deleteResponse);
            int statusCode = deleteResponse.getStatus();
            Assert.assertEquals(HttpStatus.OK.value(), statusCode);  // Validar si realmente devolvió un 200 OK
        } else {
            System.out.println("No user to delete, customerId is null");
        }

        registeredEmail = testContext().getRegisteredEmail();
        proxy = bankService.proxy;
        System.out.println(registeredEmail != null ? registeredEmail : "registeredEmail is null");

        if (registeredEmail != null) {
            // Borrar withdraws de las tarjetas registradas
            var cards = testContext().getCards();
            if (cards != null) {
                for (var card : cards) {
                    if (card != null && card.getCardId() != null) {
                        Response deleteWithdrawsResponse = proxy.deleteWithdrawsById(card.getCardId());
                        System.out.println("Delete withdraws for card " + card.getCardId() + " -> " + deleteWithdrawsResponse.getStatus());
                    }
                }
            }

            // Ejecuta y loguea cualquier borrado, validando que sea 200
            Response deleteCardsResponse = proxy.deleteCardsOfLoggedUser(null);
            System.out.println("Delete cards -> " + deleteCardsResponse.getStatus());
            assertEquals(200, deleteCardsResponse.getStatus());

            Response deleteLoggedUserResponse = proxy.deleteLoggedUser(null);
            System.out.println("Delete accounts -> " + deleteLoggedUserResponse.getStatus());
            assertEquals(200, deleteLoggedUserResponse.getStatus());

            Response deleteCustomerResponse = proxy.deleteCustomer(registeredEmail);
            System.out.println("Delete customer -> " + deleteCustomerResponse.getStatus());
            assertEquals(200, deleteCustomerResponse.getStatus());
        } else {
            System.out.println("No user to delete, registeredEmail is null");
        }
        testContext().reset();
    }

    @When("The customer deletes his customer registration by id")
    public void theCustomerDeleteHisCustomerRegistrationById() {
        Integer customerId = testContext().getOriginID();
        proxy = bankService.proxy;
        if (customerId != null) {
            Response deleteResponse = proxy.deleteCustomerById(customerId);
            testContext().setResponse(deleteResponse);
            int statusCode = deleteResponse.getStatus();
            Assert.assertEquals(HttpStatus.OK.value(), statusCode);  // Validar si realmente devolvió un 200 OK
        } else {
            System.out.println("No user to delete, customerId is null");
        }

    }

}
