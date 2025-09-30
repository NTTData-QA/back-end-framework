package org.example.api.service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.api.data.entity.Customer;
import org.example.api.data.repository.AccountRepository;
import org.example.api.data.repository.CustomerRepository;
import org.example.api.data.request.LoginRequest;
import org.example.api.data.request.UpdateRequest;
import org.example.api.token.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

  @Autowired private CustomerRepository customerRepository;
  @Autowired private AccountRepository accountRepository;
  @Autowired private AccountService accountService;
  @Autowired private AuthService authService;
  public List<Customer> findAll() {
    return customerRepository.findAll();
  }

  public Optional<Customer> findById(Integer customerId) {
    return customerRepository.findById(customerId);
  }

  public Customer save(Customer customer) { return customerRepository.save(customer);}

  public Customer register(Customer cust){
    // verify if email exists
    if(customerRepository.existsByEmail(cust.getEmail())){
      throw new IllegalArgumentException("Email already registered.");
    }
    return customerRepository.save(cust);
  }

  public Optional<Customer> findByEmail(String email) {
    return customerRepository.findByEmail(email);
  }

  public Customer getCustomerFromRequest(HttpServletRequest request){
    String jwt = authService.getJwtFromCookies(request);
    String email = Token.getCustomerEmailFromJWT(jwt);
    Optional<Customer> customerOptional =  customerRepository.findByEmail(email);
      return customerOptional.orElse(null);
  }

  public ResponseCookie updateEmailAndReturnNewCookie(UpdateRequest updateRequest, Customer customer){
    String email = updateRequest.getEmail();
    customer.setEmail(email);
    register(customer);
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail(email);
    loginRequest.setPassword(null);
    return  authService.generateJwtCookie(loginRequest);
  }

  public void updatePassword(UpdateRequest updateRequest, Customer customer){
    String password = updateRequest.getPassword();
    customer.setPassword(password);
    save(customer);
  }

  public void updateNameAndSurname(UpdateRequest updateRequest, Customer customer){
    String name = updateRequest.getName();
    String surname = updateRequest.getSurname();
    if (name != null){
      customer.setName(name);
    }
    if (surname != null){
      customer.setSurname(surname);
    }
    save(customer);
  }

  @Transactional
  public boolean deleteByEmail(String email) {
    if (customerRepository.existsByEmail(email)) {
      System.out.println("Eliminando cliente con email: " + email);
      customerRepository.deleteByEmail(email);
      return true;
    } else {
      System.out.println("Cliente no encontrado con email: " + email);
      return false;
    }
  }

  public Boolean deleteById(Integer customerId) {
    if (customerRepository.existsById(customerId)) {
      System.out.println("Eliminando cliente por su Id: " + customerId);
      customerRepository.deleteById(customerId);
      return true;
    } else {
      System.out.println("Cliente no encontrado por Id: " + customerId);
      return false;
    }
  }

  public Optional<Customer> findByPassword(String password){
    return customerRepository.findByPassword(password);
  }

//  // Map Customer  to Customer Entity
//  public Customer convertCustomerToEntity(Customer cust) {
//    Customer customer = new Customer();
//    customer.setName(cust.getName());
//    customer.setSurname(cust.getSurname());
//    customer.setEmail(cust.getEmail());
//    customer.setPassword(cust.getPassword());
//    List<Account> accounts = Collections.emptyList();
//
//    if (cust.getAccounts() == null) {
//      cust.setAccounts(Collections.emptyList()); // Lo manejamos como una lista vacía
//    }
//    else {
//      for (Account account : cust.getAccounts()) {
//        accounts.add(accountService.convertAccountToEntity(account));
//      }
//    }
//    customer.setAccounts(accounts);
//    return customer;
//  }
}