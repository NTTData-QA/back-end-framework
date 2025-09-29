package org.example.apicalls.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.example.api.data.entity.Account;
import org.example.api.data.entity.Customer;
import org.example.api.data.entity.Transfer;
import org.example.api.data.request.AccountRequest;
import org.example.api.data.request.CardRequest;
import org.example.api.data.request.LoginRequest;
import org.example.api.data.request.TransferRequest;
import org.example.api.data.request.UpdateRequest;
import org.example.apicalls.apiconfig.BankAPI;
import org.example.apicalls.client.BankClient;
import org.example.apicalls.utils.Generator;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@ComponentScan(basePackages = "org.example.apicalls.service")
public class BankService {
    private BankClient client;
    public BankAPI proxy;
    private Response response;
    NewCookie cookie;
    public BankService(){
        client = new BankClient();
        proxy = client.getAPI();
    }

    public Response getResponse(){
        return response;
    }

    public Response doRegister(String name, String surname, String email, String password, String role){
        BankAPI proxy = client.getAPI();
        Customer customer= new Customer();
        customer.setName(name);
        customer.setSurname(surname);
        customer.setEmail(email);
        customer.setPassword(password);
        if (role != null && role.equals("ADMIN")) {
            customer.setRole(Customer.UserType.ADMIN);
        } else {
            customer.setRole(Customer.UserType.USER);
        }

        response = proxy.addCustomer(customer);
        return response;
    }

    // Register a new customer randomly generated
    public Customer registerRandomCustomer(){
        Customer customer = Generator.generateRandomCustomer(0,0);
        BankAPI proxy = client.getAPI();
        response = proxy.addCustomer(customer);
        return customer;
    }

    // Register a new customer randomly generated (with n accounts, m cards)
    public Customer registerRandomCustomer(int numAccounts, int numCards){
        Customer customer = Generator.generateRandomCustomer(numCards,numAccounts);
        BankAPI proxy = client.getAPI();
        response = proxy.addCustomer(customer);
        return customer;
    }

    // Register a new customer randomly generated (with n accounts, m cards)
    public Customer registerRandomCustomer(int numAccounts, int numCards, double amount){
        Customer randCustomer = Generator.generateRandomCustomer(numCards,numAccounts,amount);
        BankAPI proxy = client.getAPI();
        response = proxy.addCustomer(randCustomer);
        String customerString = response.getHeaderString("NewCustomer");
        System.out.println(customerString);
        Customer customer = stringToCustomer(customerString);
        return customer;
    }

    public Customer stringToCustomer(String customerString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(customerString, Customer.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Response doLogin (String email, String password){

        proxy = client.getAPI();
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        response = proxy.login(loginRequest, null);
        System.out.println("HTTP Status: "+ response.getStatus());

        if (response.getStatus() == 200) {
            Map<String, NewCookie> cookies = response.getCookies();
            NewCookie newCookie = cookies.entrySet().iterator().next().getValue();
            proxy = client.getAPI(newCookie);
        }

        return response;
    }

    public Response doLogin(Customer randomCustomer){
        proxy = client.getAPI();
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(randomCustomer.getEmail());
        loginRequest.setPassword(randomCustomer.getPassword());

        response = proxy.login(loginRequest, null);
        System.out.println("HTTP Status: "+ response.getStatus());
        if (response.getStatus() == 200) {
            Map<String, NewCookie> cookies = response.getCookies();
            cookie = cookies.entrySet().iterator().next().getValue();
            proxy = client.getAPI(cookie);
        }
        return response;
    }

    public Response doLogout (){
        response = proxy.logout(null);
        System.out.println(response.getStatus());
        proxy = client.getAPI();
        return response;
    }

    // Creates a new Account (if it is for a random user, set the id on the account before calling this method)
     public Response doNewAccount (Account newAccount, HttpServletRequest request){
        response = proxy.createAccount(newAccount, request);
         System.out.println(response.getStatus());
         return response;
     }

     // Creates a new Card (if it is for a random account, set the id on the card before calling this method)
     public Response doNewCard(CardRequest newCard){
         CardRequest cardRequest = new CardRequest();
         cardRequest.setAccountId(newCard.getAccountId());
         cardRequest.setType(newCard.getType());
         response = proxy.newCard(cardRequest);
         System.out.println(response.getStatus());
         return response;
     }

     public Response doNewTransfer (TransferRequest transfer, HttpServletRequest request){
         TransferRequest transferRequest = new TransferRequest();
         transferRequest.setTransferAmount(transfer.getTransferAmount());
         if(transfer.getCurrencyType().equals("USD")){
             transferRequest.setCurrencyType(Transfer.CurrencyType.USD);
         } else{
             transferRequest.setCurrencyType(Transfer.CurrencyType.EUR);
         }
         transferRequest.setOriginAccountId(transfer.getOriginAccountId());
         transferRequest.setReceivingAccountId(transfer.getReceivingAccountId());
         response = proxy.localTransfer(transferRequest, request);
         System.out.println("Status code: " + response.getStatus());
         return response;
     }

    public Response doDeleteTransfer(Integer transferId) {
        response = proxy.deleteTransfer(transferId);
        System.out.println("Delete Transfer Status: " + response.getStatus());
        return response;
    }

    public ArrayList<Response> updateEmailAndPassword(UpdateRequest updateRequest) {
        ArrayList<Response> responses = new ArrayList<>();
        Response responseEmail = null;
        Response responsePassword = null;
        try {
            responseEmail = proxy.updateEmail(updateRequest, null);
            System.out.println(responseEmail.readEntity(String.class));
            Map<String, NewCookie> cookies = responseEmail.getCookies();
            NewCookie newCookie = cookies.entrySet().iterator().next().getValue();
            proxy = client.getAPI(newCookie);
            if (responseEmail.getStatus() == 200) {
                responsePassword = proxy.updatePassword(updateRequest, null);
                System.out.println(responsePassword.readEntity(String.class));
                if (responsePassword.getStatus() == 200) {
                    System.out.println("User credentials updated successfully.");

                } else {
                    System.out.println("Failed to update password: " + responsePassword.getStatusInfo());
                }
            } else {
                System.out.println("Failed to update email: " + responseEmail.getStatusInfo());
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
        responses.add(responseEmail);
        responses.add(responsePassword);
        return responses;
    }

    public Response getTransferHistory(int accountId) {
        return proxy.getTransferHistory(accountId);
    }

    public Response doDeleteAccountById(int accountId) {
        return proxy.deleteAccount(accountId);
    }

    public Response doDeleteAccountsByCustomerId(int customerId) {
        return proxy.deleteAccountsOfCustomer(customerId);
    }

    public Response doDeleteAccountsOfLoggedUser() {
        return proxy.deleteCardsOfLoggedUser(null);
    }

    public Response doUpdateExpirationDate(int accountId) {
        return proxy.expirationDateUpdate(accountId, null);
    }

    public Response doUpdateAccountType(int accountId, Account.AccountType accountType) {
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setAccountType(accountType);
        return proxy.accountTypeUpdate(accountId, accountRequest,null);
    }

    public Response doUpdateBlockStatus(int accountId, boolean setBlocked) {
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setIsBlocked(setBlocked);
        return proxy.isBlockUpdate(accountId, accountRequest, null);
    }

    public Response doUpdateDebtStatus(int accountId, boolean setDebt) {
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setIsBlocked(setDebt);
        return proxy.isInDebtUpdate(accountId, accountRequest, null);
    }

    public Response getAllCustomersList() { return proxy.getAllCustomers(); }

    public Response doDeleteCustomerByEmail(String email) { return proxy.deleteCustomer(email); }

    public Response getLoggedCustomer() { return proxy.getLoggedCustomer(); }
}