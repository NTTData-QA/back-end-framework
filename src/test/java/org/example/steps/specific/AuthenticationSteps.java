package org.example.steps.specific;

import io.cucumber.core.gherkin.Step;
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
import org.example.steps.utils.StepUtils;
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

    @After
    public void deleteRegisteredUser() {
    registeredEmail = testContext().getRegisteredEmail();
    proxy = bankService.proxy;
    testContext().reset();
    System.out.println(registeredEmail);
        if (registeredEmail != null) {
            response = proxy.deleteCardsOfLoggedUser(null);
            System.out.println("Status de borrar cards: "+response.getStatus());
            response = proxy.deleteLoggedUser(null);
            System.out.println("Status de borrar cuentas: "+response.getStatus());

            Response deleteResponse = proxy.deleteCustomer(registeredEmail);
            int statusCode = deleteResponse.getStatus();
            System.out.println("Delete response status code: " + statusCode);
            Assert.assertEquals(HttpStatus.OK.value(), statusCode);  // Validar si realmente devolvió un 200 OK
        } else {
            System.out.println("No user to delete, registeredEmail is null");
        }
    }
}
