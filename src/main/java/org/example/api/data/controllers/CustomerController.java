package org.example.api.data.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.example.api.data.entity.Customer;
import org.example.api.data.repository.CustomerRepository;
import org.example.api.data.request.UpdateRequest;
import org.example.api.service.AuthService;
import org.example.api.service.CustomerService;
import org.example.api.token.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class CustomerController {
  private final CustomerService customerService;
  @Autowired private CustomerRepository customerRepository;

  @Autowired
  private AuthService authService;

  @Autowired
  private Token token;

  public CustomerController(CustomerService customerService) {
    this.customerService = customerService;
  }

  @GetMapping("/api/customer/{id}")   // get 1 customer by customerId
  public Optional<Customer> customer(@PathVariable Integer id) {
    return customerService.findById(id);
  }

  @GetMapping("/api/customer/email/{email}")   // get 1 customer by customerId
  public Customer getCustomerByEmail(@PathVariable String email) {
    return customerService.findByEmail(email).get();
  }

  @GetMapping("/api/customers")      // get all customers from DB
  public List<Customer> customer(HttpServletRequest request) {
    return customerService.findAll();
  }

  @DeleteMapping("/public/customer/{email}")
  public ResponseEntity<String> deleteCustomer(@PathVariable String email) {
    try {
      boolean isDeleted = customerService.deleteByEmail(email);
      if (isDeleted) {
        return ResponseEntity.ok("Customer deleted successfully");
      } else {
        return ResponseEntity.badRequest().body("Customer not found");
      }
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Error occurred during deletion");
    }
  }

  @PatchMapping("/api/customer/update/email")
  public ResponseEntity<String> updateEmail(@RequestBody UpdateRequest updateRequest, HttpServletRequest request){
    Customer customer = customerService.getCustomerFromRequest(request);
    if (customer.equals(null)){
      return ResponseEntity.status(401).body("Not valid token. Please, log in again");
    }
    try{
      ResponseCookie jwtCookie = customerService.updateEmailAndReturnNewCookie(updateRequest,customer);
      return ResponseEntity.ok()
              .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
              .body("Email has been updated successfully");}
    catch (Exception e){
      return ResponseEntity.badRequest().body("You are using an already existing email or your email is not valid");
    }
  }

  @PatchMapping("/api/customer/update/password")
  public ResponseEntity<String> updatePassword(@RequestBody UpdateRequest updateRequest, HttpServletRequest request){
    Customer customer = customerService.getCustomerFromRequest(request);
    if (customer.equals(null)){
      return ResponseEntity.status(401).body("Not valid token. Please, log in again");
    }
    try{
      customerService.updatePassword(updateRequest, customer);
      return ResponseEntity.ok()
              .body("Password has been updated successfully");}
    catch (Exception e){
      return ResponseEntity.badRequest().body("Your password must be valid");
    }
  }

  @PatchMapping("/api/customer/update/nameandsurname")
  public ResponseEntity<String> updateNameAndPassword(@RequestBody UpdateRequest updateRequest, HttpServletRequest request){
    Customer customer = customerService.getCustomerFromRequest(request);
    if (customer.equals(null)){
      return ResponseEntity.status(401).body("Not valid token. Please, log in again");
    }
    try{
      customerService.updateNameAndSurname(updateRequest, customer);
      return ResponseEntity.ok()
              .body("Name and Surname have been updated successfully");}
    catch (Exception e){
      return ResponseEntity.badRequest().body("Your name or surname must be valid");
    }
  }

  @DeleteMapping("/public/customer/delete/{customerId}")
  public ResponseEntity<String> deleteById(@PathVariable Integer customerId) {
    Optional<Customer> customer = customerRepository.findById(customerId);
    if (customer.isEmpty()){
      return ResponseEntity.badRequest().body("Error: customer not found");
    }

//    List<Account> accounts = customer.get().getAccounts();
//    if (accounts.isEmpty()) {
//      return ResponseEntity.status(404).body("Error: No accounts found for this customer");
//    }

//    try {
//      for (Account account : accounts) {
//        // Delete transfers where the account is the origin account
//        List<Transfer> originTransfers = transferRepository.findByOriginAccount_AccountId(account.getAccountId());
//        for (Transfer transfer : originTransfers) {
//          Account originAccount = transfer.getOriginAccount();
//          if (originAccount != null) {
//            originAccount.getOriginatingTransfers().remove(transfer);
//          }
//          transferRepository.delete(transfer);
//        }

    // Delete transfers where the account is the receiving account
//        List<Transfer> receivingTransfers = transferRepository.findByReceivingAccount_AccountId(account.getAccountId());
//        for (Transfer transfer : receivingTransfers) {
//          Account receivingAccount = transfer.getReceivingAccount();
//          if (receivingAccount != null) {
//            receivingAccount.getReceivingTransfers().remove(transfer);
//          }
//          transferRepository.delete(transfer);
//        }
//      }

    // Delete all the accounts associated with the customer
    customerRepository.deleteById(customerId);
//      customer.get().deleteAllAccounts();

    return ResponseEntity.ok("The customer has been deleted successfully.");
//    } catch (Exception e) {
//      return ResponseEntity.status(500).body("Error: Could not delete accounts. " + e.getMessage());
//    }
  }
}

