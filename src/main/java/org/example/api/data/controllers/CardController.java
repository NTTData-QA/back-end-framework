package org.example.api.data.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.example.api.data.entity.Account;
import org.example.api.data.entity.Card;
import org.example.api.data.request.CardRequest;
import org.example.api.service.AccountService;
import org.example.api.service.AuthService;
import org.example.api.service.CustomerService;
import org.example.api.token.Token;
import org.example.apicalls.utils.Generator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.api.data.entity.Customer;
import org.example.api.service.CardService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@RestController
public class CardController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AuthService authenticationService;

    @Autowired
    private Token tokenService;

    private final CardService card;

    public CardController(CardService card) {
        this.card = card;
    }

    @GetMapping("api/BDcards")     // get all cards from DB
    public List<Card> card() {
        return card.findAll();
    }

    @GetMapping("api/card/{cardId}")       // get 1 card by cardId
    public Optional<Card> card(@PathVariable Integer id) {
        return card.findById(id);
    }

    @GetMapping("api/cards/{accountId}")    // get all cards by accountId
    public List<Card> cardsByAccountId(@PathVariable Integer accountId) {
        return card.findByAccountId(accountId);
    }

    @PostMapping("/api/card/new")
    public ResponseEntity<String> newCard(@RequestBody CardRequest cardRequest) {
        // Verify user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Assuming username is contained in the customerId
        Integer customerId = Integer.valueOf(authentication.getName());

        // If we could not find the customer -> you are not logged
        if(!customerService.findById(customerId).isPresent()){
            return ResponseEntity.badRequest().body("Error creating card: You must be logged");
        }

        // Verifying correct data from user
        if (!cardRequest.getType().equals("Debit") && !cardRequest.getType().equals("Credit")) {
            return ResponseEntity.badRequest().body("Error creating card: card type not valid");
        }

        // Generate randomly CVC and CardNumber
        Random rnd = new Random();
        Long number = rnd.nextLong() % 10000000000000000L;
        int cvc = rnd.nextInt() % 1000;
        // We dont want negative CVC numbers
        if (cvc < 0) cvc *= -1;

        // Create new card
        Card newCard = new Card();
        newCard.setType(cardRequest.getType());
        newCard.setExpirationDate(Generator.generateRandomFutureDate());
        newCard.setNumber(number);
        newCard.setCvc(cvc);

        // Find account that will contain card
        Optional<Account> account = accountService.findById(cardRequest.getAccountId());

        // Verifying correct account data
        if (!account.isPresent()) {
            return ResponseEntity.badRequest().body("Error creating card: account not found");
        }

        newCard.setAccount(account.get());
        card.save(newCard);

        return ResponseEntity.ok("Card created successfully");
    }
    @GetMapping("/api/cards")   // get all cards from a customer
    public List<Card> getCards(@AuthenticationPrincipal Customer userDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Assuming username contains customerId
        Integer customerId = Integer.valueOf(authentication.getName());

        return card.getCardsByCustomerId(customerId);

    }

    @DeleteMapping("/api/card/delete/{cardId}")
    public ResponseEntity<String> deleteCard(@PathVariable int cardId){
        // Check if the card exists
        Optional<Card> card = this.card.findById(cardId);
        if (card.isEmpty())
            return ResponseEntity.badRequest().body("Error: card does not exist");

        Account account = card.get().getAccount();
        if (account == null)
            return ResponseEntity.badRequest().body("Error: account not found");

        account.deleteCard(cardId);
        this.card.deleteById(cardId);

        return ResponseEntity.ok("Card deleted successfully");
    }

    @DeleteMapping("/api/card/delete/{accountId}")
    public ResponseEntity<String> deleteCardsOfAccounts(@PathVariable int accountId){
        // Check if account exists
        Optional<Account> account = accountService.findById(accountId);
        if (account.isEmpty())
            return ResponseEntity.badRequest().body("Error: account does not exist");

        // Delete all cards of this account
        this.card.deleteCardsByAccount(accountId);
        account.get().deleteAllCards();

        return ResponseEntity.ok("Account cards deleted successfully");
    }

    @DeleteMapping("/api/card/delete/{customerId}")
    public ResponseEntity<String> deleteCardsOfCustomer(@PathVariable int customerId){
        // Check if customer exists
        Optional<Customer> customer = customerService.findById(customerId);
        if (customer.isEmpty())
            return ResponseEntity.badRequest().body("Error: customer does not exist");

        // We delete every card of the customer (from each account)
        List<Account> accounts = customer.get().getAccounts();
        for (Account account : accounts){
            this.card.deleteCardsByAccount(account.getAccountId());
        }

        // We iterate across the accounts and delete
//        int i = 0;
//        boolean error = false;
//        ResponseEntity<String> response = null;
//        while (i < accounts.size() && !error){
//            response = deleteCardsOfAccounts(accounts.get(i).getAccountId());
//            error = response.getStatusCode() == HttpStatusCode.valueOf(400);
//            i++;
//        }
//        if (error) return response;

        return ResponseEntity.ok("Customer cards deleted successfully");
    }

    @DeleteMapping("/api/card/delete")
    public ResponseEntity<String> deleteCardsOfLoggedUser(HttpServletRequest request){
        // Get the customer logged
        String jwt = authenticationService.getJwtFromCookies(request);
        System.out.println(jwt);

        // Validate token
        if (jwt == null || !tokenService.validateToken(jwt)) {
            return ResponseEntity.status(401).build(); // 401 Unauthorized if token is not valid
        }

        // Get user email from token
        String email = tokenService.getCustomerEmailFromJWT(jwt);

        // Get user from email
        Optional<Customer> customerOpt = authenticationService.findCustomerByEmail(email);
        if (!customerOpt.isPresent())
            return ResponseEntity.status(404).build(); // 404 Not Found if user is not found

        ResponseEntity<String> response = deleteCardsOfCustomer(customerOpt.get().getCustomerId());
        if (response.getStatusCode() == HttpStatusCode.valueOf(400))
            return response;
        else return ResponseEntity.ok("Logged user cards deleted successfully");
    }

}
