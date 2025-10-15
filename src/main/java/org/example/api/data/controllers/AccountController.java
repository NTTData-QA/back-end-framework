package org.example.api.data.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.example.api.data.entity.Account;
import org.example.api.data.entity.Customer;
import org.example.api.data.entity.Transfer;
import org.example.api.data.repository.AccountRepository;
import org.example.api.data.repository.CustomerRepository;
import org.example.api.data.repository.TransferRepository;
import org.example.api.data.request.AccountRequest;
import org.example.api.data.request.UpdateRequest;
import org.example.api.service.AccountService;
import org.example.api.service.AuthService;
import org.example.api.service.CustomerService;
import org.example.api.token.Token;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController

public class AccountController {


    @Autowired private AccountService accountService;
    @Autowired private AuthService authService;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private Token tokenService;
    @Autowired private CustomerService customerService;
    @Autowired private TransferRepository transferRepository;
    @Autowired private CardController cardController;


    @GetMapping("/api/account/{id}")    // get 1 account by accountId
    public Optional<Account> accountById(@PathVariable Integer id) {
        return accountService.findById(id);
    }

    @GetMapping("/api/accounts/{customerId}")   // get all accounts by customerId
    public List<Account> accountsByCustomer(@PathVariable Integer customerId) {
        return accountService.findByCustomer(customerId);
    }

    @GetMapping("/api/accounts")    // get all accounts from the logged-in user
    public ResponseEntity<List<Account>> getUserAccounts(HttpServletRequest request) {
        // Get JWT token from cookies
        String jwt = authService.getJwtFromCookies(request);
        System.out.println(jwt);

        // Validate token
        if (jwt == null || !tokenService.validateToken(jwt)) {
            return ResponseEntity.status(401).build(); // 401 Unauthorized if token is not valid
        }

        // Get user mail from token
        String email = Token.getCustomerEmailFromJWT(jwt);

        // Get user from email
        Optional<Customer> customerOpt = authService.findCustomerByEmail(email);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(404).build(); // 404 Not Found if error finding user
        }

        // Get logged user´s accounts
        Customer customer = customerOpt.get();
        List<Account> accounts = accountService.findByCustomer(customer.getCustomerId());

        return ResponseEntity.ok(accounts); // 200 OK with users accounts
    }

    @GetMapping ("/api/accounts/amount")    // get total amount from all accounts (logged-in user)
    public ResponseEntity<Double> getUserAmount(HttpServletRequest request){
        // Get JWT token from cookies
        String jwt = authService.getJwtFromCookies(request);
        System.out.println(jwt);

        // Validate token
        if (jwt == null || !tokenService.validateToken(jwt)) {
            return ResponseEntity.status(401).build(); // 401 Unauthorized if token is not valid
        }

        // Get user email from token
        String email = Token.getCustomerEmailFromJWT(jwt);

        // Get user from email
        Optional<Customer> customerOpt = authService.findCustomerByEmail(email);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(404).build(); // 404 Not Found if user is not found
        }

        // Get logged user´s accounts
        Customer customer = customerOpt.get();
        List<Account> accounts = accountService.findByCustomer(customer.getCustomerId());

        // Calculate total money from all accounts
        double totalAmount = 0.0;
        for (Account acc : accounts){
            totalAmount += acc.getAmount();
        }

        return ResponseEntity.ok(totalAmount); // 200 OK with total money
    }

    private boolean checkAccountInDebt(Account account){
        return account.getAmount() < 0;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/amount/{accountId}")  // get amount by accountId
    public Double amountOfAccount(@PathVariable Integer accountId) {
        return accountService.findById(accountId).orElseThrow().getAmount();
    }

    @PostMapping("/api/account/new")
    public ResponseEntity<String> createAccount(@RequestBody Account newAccount, HttpServletRequest request) {
        // Obtener el token JWT de las cookies
        String jwt = authService.getJwtFromCookies(request);

        // Validar el token
        if (jwt == null || !tokenService.validateToken(jwt)) {
            return ResponseEntity.status(401).body("Unauthorized: Token is not valid."); // 401 Unauthorized
        }

        // Obtener el email del usuario a partir del token
        String email = Token.getCustomerEmailFromJWT(jwt);

        // Obtener el cliente usando el email
        Optional<Customer> customerOpt = authService.findCustomerByEmail(email);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Error: User not found."); // 404 Not Found
        }

        // Asignar el cliente a la nueva cuenta
        Customer customer = customerOpt.get();
        newAccount.setCustomer(customer);

        // Asignar creationDate a la hora y fecha actual
        newAccount.setCreationDate(LocalDateTime.now());

        // Asignar expirationDate, por ejemplo, un año después de la creationDate
        newAccount.setExpirationDate(newAccount.getCreationDate().plusYears(1)); // Ajusta según tu lógica de negocio

        // isBlocked debe ser FALSE para una nueva cuenta
        newAccount.setIsBlocked(false);

        // isInDebt depende del amount, si es mayor a 0 se considera que no está en deuda
        // Está en deuda si el amount es 0 o negativo
        newAccount.setIsInDebt(newAccount.getAmount() == null || newAccount.getAmount() <= 0); // No está en deuda

        // Asignar el accountType si está presente, de lo contrario, asignar un tipo por defecto
        if (newAccount.getAccountType() != null) {
            newAccount.setAccountType(newAccount.getAccountType());
        } else {
            newAccount.setAccountType(Account.AccountType.CHECKING_ACCOUNT); // Establecer un tipo predeterminado
        }

        try {
            // Guardar la nueva cuenta en el repositorio
            Account createdAccount = accountService.save(newAccount);
            return ResponseEntity.status(201).body("Account created successfully: " + createdAccount.getAccountId()); // 201 Created
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: Could not create account. " + e.getMessage()); // 500 Internal Server Error
        }
    }

    //Delete an account with its Id
    @DeleteMapping ("/api/account/delete/{accountId}")
    public ResponseEntity<String> deleteAccount(@PathVariable int accountId){
        // Check if the account exists
        Optional<Account> accountOptional = accountRepository.findByAccountId(accountId);
        if (accountOptional.isEmpty()){
            return ResponseEntity.badRequest().body("Error: account not found");
        }
        Account account = accountOptional.get();

        String errores = checkAccountStatus(account);
        if (!errores.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: " + errores);
        }

        // Check if the account's customer exists
        Customer customer = account.getCustomer();
        if (customer == null){
            return ResponseEntity.badRequest().body("Error: client of the account not found");
        }

        // Get all the transfers
        List<Transfer> originTransfers = transferRepository.findByOriginAccount_AccountId(accountId);
        List<Transfer> receivingTransfers = transferRepository.findByReceivingAccount_AccountId(accountId);

        //Delete origin transfers
        for (Transfer transfer : originTransfers) {
            // Delete transfer of origin account
            Account originAccount = transfer.getOriginAccount();
            if (originAccount != null) {
                originAccount.getOriginatingTransfers().remove(transfer);
            }
            transferRepository.delete(transfer);
        }

        //Delete receiving transfers
        for (Transfer transfer : receivingTransfers) {
            // Eliminar la transferencia de la cuenta de origen
            Account receivinAccount = transfer.getReceivingAccount();
            if (receivinAccount != null) {
                receivinAccount.getReceivingTransfers().remove(transfer);
            }
            transferRepository.delete(transfer);
        }


        // We try to delete the account from the customer list and from the database
        if (!customer.deleteAccount(accountId)){
            return ResponseEntity.badRequest().body("Error: could not delete account from customer");
        }
        accountRepository.delete(account);

        return ResponseEntity.ok("Account deleted successfully");
    }

    @NotNull
    private static String checkAccountStatus(Account account) {
        String errores = "";
        boolean enDeuda = false;
        if (account.getIsInDebt()) {
            errores = errores + "Account with id " + account.getAccountId() + " is in debt";
            enDeuda = true;
        }
        if (account.getIsBlocked()) {
            if (enDeuda) errores = errores + " and blocked";
            else errores = errores + "Account with id " + account.getAccountId() + " is blocked";
        }
        return errores;
    }

    //Delete all the accounts with its customer id
    @Transactional
    @DeleteMapping("/api/account/delete/customer/{customerId}")
    public ResponseEntity<String> deleteAccountsOfCustomer(@PathVariable int customerId){
        // Check if the customer exists
        Optional<Customer> customerOptional = customerRepository.findById(customerId);
        if (customerOptional.isEmpty()){
            return ResponseEntity.badRequest().body("Error: customer not found");
        }
        Customer customer = customerOptional.get();

        // We get all customers accounts
        List<Account> accounts = customer.getAccounts();
        if (accounts.isEmpty()) {
            return ResponseEntity.status(404).body("Error: No accounts found for this customer");
        }

        List<String> errorAccounts = new ArrayList<>();
        // Try to delete the associated transfers and accounts
        try {
            // Iteramos sobre una copia (evita ConcurrentModificationException)
            for (Account account : new ArrayList<>(accounts)) {
                // Check if account is in debt or blocked
                String errores = checkAccountStatus(account);
                if (!errores.isEmpty()) {
                    errorAccounts.add(errores);
                    continue;
                }

                // Delete transfers where the account is the origin account
                List<Transfer> originTransfers = transferRepository.findByOriginAccount_AccountId(account.getAccountId());
                for (Transfer transfer : originTransfers) {
                    Account originAccount = transfer.getOriginAccount();
                    if (originAccount != null) {
                        originAccount.getOriginatingTransfers().remove(transfer);
                    }
                    transferRepository.delete(transfer);
                }

                // Delete transfers where the account is the receiving account
                List<Transfer> receivingTransfers = transferRepository.findByReceivingAccount_AccountId(account.getAccountId());
                for (Transfer transfer : receivingTransfers) {
                    Account receivingAccount = transfer.getReceivingAccount();
                    if (receivingAccount != null) {
                        receivingAccount.getReceivingTransfers().remove(transfer);
                    }
                    transferRepository.delete(transfer);
                }

                // Delete cards of the account. This also remove the witdraws of each card
                cardController.deleteCardsOfAccounts(account.getAccountId());

                customer.deleteAccount(account.getAccountId());
                accountRepository.delete(account);
            }

            if (!errorAccounts.isEmpty()) {
                StringBuilder finalBuilder = new StringBuilder(
                        "All accounts and associated transfers have been deleted seccesfully EXCEPT FOR THE FOLLOWING:"
                );
                for (String e: errorAccounts) {
                    finalBuilder.append("\n\t").append(e);
                }
                String finalMsg = finalBuilder.toString();
                return ResponseEntity.status(409).body(finalMsg);
            }
            return ResponseEntity.ok("All accounts and associated transfers have been deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/api/account/delete")
    public ResponseEntity<String> deleteLoggedUserAccounts (HttpServletRequest request){
        // Get JWT token from cookies
        String jwt = authService.getJwtFromCookies(request);
        System.out.println(jwt);

        // Validate token
        if (jwt == null || !tokenService.validateToken(jwt)) {
            return ResponseEntity.status(401).build(); // 401 Unauthorized if token is not valid
        }

        // Get user email from token
        String email = Token.getCustomerEmailFromJWT(jwt);

        // Get user from email
        Optional<Customer> customerOpt = authService.findCustomerByEmail(email);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(404).build(); // 404 Not Found if user is not found
        }

        // Get logged user´s accounts
        Customer customer = customerOpt.get();
        List<Account> accounts = accountService.findByCustomer(customer.getCustomerId());

        List<String> errorAccounts = new ArrayList<>();
        //Try to delete the associated transfers and the accounts
        try {
            for (Account acc : accounts) {
                String errores = checkAccountStatus(acc);
                if (!errores.isEmpty()) {
                    errorAccounts.add(errores);
                    continue;
                }

                // Eliminar las transferencias donde la cuenta es la cuenta de origen
                transferRepository.deleteByOriginAccount_AccountId(acc.getAccountId());
                // Eliminar las transferencias donde la cuenta es la cuenta de destino
                transferRepository.deleteByReceivingAccount_AccountId(acc.getAccountId());

                customer.deleteAccount(acc.getAccountId());
                accountRepository.delete(acc);
            }

            if (!errorAccounts.isEmpty()) {
                StringBuilder finalBuilder = new StringBuilder(
                        "All accounts and associated transfers have been deleted seccesfully EXCEPT FOR THE FOLLOWING:"
                );
                for (String e: errorAccounts) {
                    finalBuilder.append("\n\t").append(e);
                }
                String finalMsg = finalBuilder.toString();
                throw new RuntimeException(finalMsg);
            }
            return ResponseEntity.ok("All accounts and associated transfers have been deleted successfully."); // 200 OK
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage()); // 500 Internal Server Error
        }
    }

    @PatchMapping("/api/account/deposit/{accountId}")
    public ResponseEntity<String> depositAccountId(@PathVariable Integer accountId, @RequestBody UpdateRequest updateRequest){

        Optional<Account> accountOpt = accountRepository.findByAccountId(accountId);
        if (accountOpt.isEmpty()){
            return ResponseEntity.badRequest().body("There is no account with ID: "+ accountId);
        }

        Account account= accountOpt.get();
        Double deposit = updateRequest.getAmount();
        if (deposit <= 0){
            return ResponseEntity.badRequest().body("The deposit must be greater than 0");
        }

        try{
            accountService.makeDeposit(account,deposit);
            return ResponseEntity.ok().body("The deposit was made successfully");
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("The deposit could not be done");
        }
    }

//    @PatchMapping("/api/account/withdraw/{accountId}")
//    public ResponseEntity<String> withdrawAccountId(@PathVariable Integer accountId, @RequestBody UpdateRequest updateRequest, HttpServletRequest request){
//    DOCUMENTACIÓN: Sustituido por otro endpoint

    @PatchMapping("/api/account/update/expirationDate/{accountId}")
    public ResponseEntity<String> expirationDateUpdate(@PathVariable Integer accountId, HttpServletRequest request) {
        Optional<Account> accountOpt = accountRepository.findByAccountId(accountId);
        if (accountOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("There is no account with ID:" + accountId);
        }
        Account account = accountOpt.get();
        Customer customerAccount = customerService.getCustomerFromRequest(request);
        LocalDateTime expirationDateLimit = LocalDateTime.now().plusMonths(3);
        LocalDateTime expirationDateExtension = LocalDateTime.now().plusYears(5);

        if (customerAccount != account.getCustomer()) {
            return ResponseEntity.badRequest().body("You cannot extend the expiration date of an account that is not associated with you.");
        }

        if (!account.getExpirationDate().isBefore(expirationDateLimit)) {
            return ResponseEntity.badRequest().body("You can only extend the expiration date if it is within the next 3 months.");
        }

        account.setExpirationDate(expirationDateExtension);
        accountService.save(account);
        return ResponseEntity.ok("The expiration date has been updated to:" + expirationDateExtension);
    }

    @PatchMapping("/api/account/update/accountType/{accountId}")
    public ResponseEntity<String> accountTypeUpdate(@PathVariable Integer accountId, @RequestBody AccountRequest accountRequest, HttpServletRequest request) {

        Optional<Account> accountOpt = accountRepository.findByAccountId(accountId);
        if (accountOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("There is no account with ID: " + accountId);
        }

        Account account = accountOpt.get();
        Customer customerAccount = customerService.getCustomerFromRequest(request);
        Account.AccountType oldAccountType = account.getAccountType();

        if (oldAccountType.equals(accountRequest.getAccountType())) {
            return ResponseEntity.ok().body("The account already has the requested account type. No changes were made");
        }

        Account.AccountType accountType = accountRequest.getAccountType();

        if (!customerAccount.equals(account.getCustomer())) {
            return ResponseEntity.badRequest().body("You cannot change accountType of an account that is not associated with you.");
        }

        if (accountType == null) {
            return ResponseEntity.badRequest().body("Account type cannot be null.");
        }

        account.setAccountType(accountType);
        accountService.save(account);
        return ResponseEntity.ok("Your accountType has been changed");
    }

    @PatchMapping("/api/account/update/isBlocked/{accountId}")
    public ResponseEntity<String> isBlockUpdate(@PathVariable Integer accountId, @RequestBody AccountRequest accountRequest, HttpServletRequest request) {

        Optional<Account> accountOpt = accountRepository.findByAccountId(accountId);
        if (accountOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("There is no account with ID: " + accountId);
        }

        Account account = accountOpt.get();
        Customer customerAccount = customerService.getCustomerFromRequest(request);
        Boolean blockedStatus = account.getIsBlocked();

        if (blockedStatus.equals(accountRequest.getIsBlocked())) {
            return ResponseEntity.ok().body("The account already has the requested blocked status. No changes were made");
        }

        Boolean isBlocked = accountRequest.getIsBlocked();

        if (!customerAccount.equals(account.getCustomer())) {
            return ResponseEntity.badRequest().body("You cannot change blocked status of an account that is not associated with you.");
        }

        if (isBlocked == null) {
            return ResponseEntity.badRequest().body("Blocked status cannot be null.");
        }

        account.setIsBlocked(isBlocked);
        accountService.save(account);
        return ResponseEntity.ok("Your blocked status has been changed");
    }
}
