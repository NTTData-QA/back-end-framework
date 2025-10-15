package org.example.api.data.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.example.api.data.entity.Account;
import org.example.api.data.entity.Card;
import org.example.api.data.entity.Withdraw;
import org.example.api.data.repository.AccountRepository;
import org.example.api.data.repository.CardRepository;
import org.example.api.data.repository.CustomerRepository;
import org.example.api.data.request.CardRequest;
import org.example.api.data.request.UpdateRequest;
import org.example.api.service.AccountService;
import org.example.api.service.AuthService;
import org.example.api.service.CustomerService;
import org.example.api.token.Token;
import org.example.apicalls.utils.Generator;
import org.example.apicalls.utils.JsonConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.api.data.entity.Customer;
import org.example.api.service.CardService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.List;
import java.util.Optional;

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

    @Autowired private WithdrawController withdrawController;

    @Autowired private CardRepository cardRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired  private CardService cardService;

    @Autowired
    private AuthService authService;

    private final CardService card;

    public CardController(CardService card) {
        this.card = card;
    }

    @GetMapping("api/BDcards")     // get all cards from DB
    public List<Card> card() {
        return card.findAll();
    }

    @GetMapping("api/card/{cardId}")       // get 1 card by cardId
    public Optional<Card> cardById (@PathVariable Integer cardId) {
        return card.findById(cardId);
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
        if(customerService.findById(customerId).isEmpty()){
            return ResponseEntity.badRequest().body("Error creating card: You must be logged");
        }

        // Verifying correct data from user
        if (!cardRequest.getType().equals(Card.CardType.CREDIT) && !cardRequest.getType().equals(Card.CardType.DEBIT)) {
            return ResponseEntity.badRequest().body("Error creating card: card type not valid");
        }

        // Find account that will contain card
        Optional<Account> account = accountService.findById(cardRequest.getAccountId());

        // Verifying correct account data
        if (account.isEmpty()) {
            return ResponseEntity.badRequest().body("Error creating card: account not found");
        }
        //verificar que la cuenta pertenece al usuario logeado
        List<Account> lista = accountRepository.findByCustomer_CustomerId(customerId);
        List<Integer> lista1 = new ArrayList<>();
        for (Account r : lista){
            lista1.add(r.getAccountId());
        }
        if(!lista1.contains(cardRequest.getAccountId())){
            return ResponseEntity.badRequest().body("Error creating card: you can only create card to your account");
        }

        Card newCard = Generator.generateCardType(account.get(), cardRequest);
        card.save(newCard);

        return ResponseEntity.ok("Card created successfully");
    }
    
    @GetMapping("/api/cards")   // get all cards from logged user
    public ResponseEntity<String> getLoggedUserCards() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Assuming username contains customerId
        Integer customerId = Integer.valueOf(authentication.getName());
        List<Card> cards = card.getCardsByCustomerId(customerId);
        JsonConverter jsonConverter = new JsonConverter();
        String jsonOutput = jsonConverter.convertListToJson(cards);
        return ResponseEntity.ok().body(jsonOutput);
    }

    @PatchMapping("/api/card/update/dailyLimit/{cardId}")
    public ResponseEntity<String> updateDailyLimit(@PathVariable Integer cardId, @RequestBody UpdateRequest updateRequest, HttpServletRequest request){

        Optional<Card> cardOpt = cardRepository.findById(cardId);
        if (!cardOpt.isPresent()){
            return ResponseEntity.badRequest().body("There is no card with ID: "+ cardId);
        }

        Card card = cardOpt.get();
        Double newDailyLimit = updateRequest.getDailyLimit();

        // Get request client
        String jwt = authService.getJwtFromCookies(request);
        String email = Token.getCustomerEmailFromJWT(jwt);
        Customer customer = customerRepository.findByEmail(email).get();

        Account account = card.getAccount();
        if(!Objects.equals(account.getCustomer().getCustomerId(), customer.getCustomerId()) && !customer.getRole().equals(Customer.UserType.ADMIN)){
            return ResponseEntity.status(403).body("Card does not belong to the user");
        }

        if(newDailyLimit <= 0){
            return ResponseEntity.badRequest().body("The new daily limit must be greater than 0");
        }

        try{
            cardService.updateDailyLimit(card, newDailyLimit);
            return ResponseEntity.ok()
                    .body("The new daily limit has been updated successfully");}
        catch (Exception e){
            return ResponseEntity.badRequest().body("Your daily limit must be valid");
        }
    }


    @PatchMapping("/api/card/update/monthlyLimit/{cardId}")
    public ResponseEntity<String> updateMonthlyLimit(@PathVariable Integer cardId, @RequestBody UpdateRequest updateRequest, HttpServletRequest request){

        Optional<Card> cardOpt = cardRepository.findById(cardId);
        if (!cardOpt.isPresent()){
            return ResponseEntity.badRequest().body("There is no card with ID: "+ cardId);
        }

        Card card = cardOpt.get();
        Double newMonthlyLimit = updateRequest.getMonthlyLimit();

        // Get request client
        String jwt = authService.getJwtFromCookies(request);
        String email = Token.getCustomerEmailFromJWT(jwt);
        Customer customer = customerRepository.findByEmail(email).get();

        Account account = card.getAccount();
        if(!Objects.equals(account.getCustomer().getCustomerId(), customer.getCustomerId()) && !customer.getRole().equals(Customer.UserType.ADMIN)){
            return ResponseEntity.status(403).body("Card does not belong to the user");
        }

        if(newMonthlyLimit <= 0){
            return ResponseEntity.badRequest().body("The new monthly limit must be greater than 0");
        }

        try{
            cardService.updateMonthlyLimit(card, newMonthlyLimit);
            return ResponseEntity.ok()
                    .body("The new monthly limit has been updated successfully");}
        catch (Exception e){
            return ResponseEntity.badRequest().body("Your monthly limit must be valid");
        }
    }
    @PatchMapping("/api/card/update/isBlocked/{cardId}")
    public ResponseEntity<String> updateisBlocked(@PathVariable Integer cardId, @RequestBody UpdateRequest updateRequest, HttpServletRequest request){

        Optional<Card> cardOpt = cardRepository.findById(cardId);
        if (!cardOpt.isPresent()){
            return ResponseEntity.status(404).body("There is no card with ID: "+ cardId);
        }

        Card card = cardOpt.get();
        Boolean newIsBlocked = updateRequest.getIsBlocked();

        // Get request client
        String jwt = authService.getJwtFromCookies(request);
        String email = Token.getCustomerEmailFromJWT(jwt);
        Customer customer = customerRepository.findByEmail(email).get();

        Account account = card.getAccount();
        if(!Objects.equals(account.getCustomer().getCustomerId(), customer.getCustomerId()) && !customer.getRole().equals(Customer.UserType.ADMIN)){
            return ResponseEntity.status(403).body("Card does not belong to the user");
        }

        if (newIsBlocked == null) {
            return ResponseEntity.badRequest().body("Blocked status cannot be null.");
        }

        try{
            cardService.updateIsBlocked(card, newIsBlocked);
            return ResponseEntity.ok()
                    .body("The block state has been updated successfully");}
        catch (Exception e){
            return ResponseEntity.badRequest().body("Error updating block state");
        }
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

    @DeleteMapping("/api/card/delete/account/{accountId}")
    public ResponseEntity<String> deleteCardsOfAccounts(@PathVariable int accountId){
        // Check if account exists
        Optional<Account> account = accountService.findById(accountId);
        if (account.isEmpty())
            return ResponseEntity.badRequest().body("Error: account does not exist");

        //Delete all withdraws of each card
        List<Card> cards = account.get().getCards();
        for (Card card: cards){
            withdrawController.deleteWithdrawsById(card.getCardId());
        }

        // Delete all cards of this account
        this.card.deleteCardsByAccount(accountId);
        account.get().deleteAllCards();

        return ResponseEntity.ok("Account cards deleted successfully");
    }

    @DeleteMapping("/api/card/delete/customer/{customerId}")
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
        return ResponseEntity.ok("Customer cards deleted successfully");
    }

    @DeleteMapping("/api/card/delete")
    public ResponseEntity<String> deleteLoggedUserCards(HttpServletRequest request){
        // Get the customer logged
        String jwt = authenticationService.getJwtFromCookies(request);
        System.out.println(jwt);

        // Validate token
        if (jwt == null || !tokenService.validateToken(jwt)) {
            return ResponseEntity.status(401).build(); // 401 Unauthorized if token is not valid
        }

        // Get user email from token
        String email = Token.getCustomerEmailFromJWT(jwt);

        // Get user from email
        Optional<Customer> customerOpt = authenticationService.findCustomerByEmail(email);
        if (customerOpt.isEmpty())
            return ResponseEntity.status(404).build(); // 404 Not Found if user is not found

        ResponseEntity<String> response = deleteCardsOfCustomer(customerOpt.get().getCustomerId());
        if (response.getStatusCode() == HttpStatusCode.valueOf(400))
            return response;
        else return ResponseEntity.ok("Logged user cards deleted successfully");
    }
}
