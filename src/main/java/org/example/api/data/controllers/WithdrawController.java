package org.example.api.data.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.example.api.data.entity.Account;
import org.example.api.data.entity.Card;
import org.example.api.data.entity.Customer;
import org.example.api.data.entity.Withdraw;
import org.example.api.data.repository.CardRepository;
import org.example.api.service.*;
import org.example.api.token.Token;
import org.example.api.data.request.WithdrawRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class WithdrawController {



    private final WithdrawService withdrawService;
    private final CardRepository cardRepository;
    private final AuthService authService;
    private final CustomerService customerService;
    private final Token tokenService;
    private final CardService cardService;
    private final AccountService accountService;

    public WithdrawController(WithdrawService withdrawService,
                              CardRepository cardRepository,
                              AuthService authService,
                              CustomerService customerService,
                              Token tokenService,
                              CardService cardService,
                              AccountService accountService) {
        this.withdrawService = withdrawService;
        this.cardRepository = cardRepository;
        this.authService = authService;
        this.customerService = customerService;
        this.tokenService = tokenService;
        this.cardService = cardService;
        this.accountService = accountService;
    }

    // Crear withdraw por tarjeta (AUTENTICADO)
    @PostMapping("/api/withdraw")
    public ResponseEntity<?> create(@RequestBody WithdrawRequest req,
                                    HttpServletRequest request) {
        // 1) Autenticación por cookie JWT (como en otros controladores)
        String jwt = authService.getJwtFromCookies(request);
        if (jwt == null || !tokenService.validateToken(jwt)) {
            return ResponseEntity.status(401).body("Unauthorized: Token is not valid.");
        }
        String email = tokenService.getCustomerEmailFromJWT(jwt);
        Optional<Customer> customerOpt = authService.findCustomerByEmail(email);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Error: User not found.");
        }
        Integer authCustomerId = customerOpt.get().getCustomerId();

        // 2) Validaciones básicas
//        if (req.getAmount() == null || req.getAmount() <= 0) {
//            return ResponseEntity.badRequest().body("Amount must be greater than 0");
//        }
        if (req.getCardId() == null) {
            return ResponseEntity.badRequest().body("cardId is required");
        }

        // 3) Cargar la tarjeta y verificar propiedad
        Optional<Card> cardOpt = cardRepository.findById(req.getCardId());
        if (cardOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Card not found");
        }
        Card card = cardOpt.get();

        Integer ownerId = card.getAccount().getCustomer().getCustomerId();
        if (!ownerId.equals(authCustomerId)) {
            return ResponseEntity.status(403).body("Forbidden: card does not belong to the authenticated customer");
        }

        // (Opcional) Si añadiste isBlocked en Card/Account, valida aquí:
        // if (Boolean.TRUE.equals(card.getIsBlocked())) return ResponseEntity.badRequest().body("Card is blocked");

        try {
            Withdraw w = withdrawService.createWithdraw(card, req.getAmount(), req.getWithdrawDate());
            return ResponseEntity.ok(w);
        } catch (IllegalStateException ise) {
            return ResponseEntity.badRequest().body(ise.getMessage());
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(iae.getMessage());
        }
    }

    // Listar withdraws del cliente autenticado
    @GetMapping("/api/withdraws")
    public ResponseEntity<?> listMine(HttpServletRequest request) {
        String jwt = authService.getJwtFromCookies(request);
        if (jwt == null || !tokenService.validateToken(jwt)) {
            return ResponseEntity.status(401).body("Unauthorized: Token is not valid.");
        }
        String email = tokenService.getCustomerEmailFromJWT(jwt);
        Optional<Customer> customerOpt = authService.findCustomerByEmail(email);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Error: User not found.");
        }
        List<Withdraw> result = withdrawService.findByCustomer(customerOpt.get().getCustomerId());
        return ResponseEntity.ok(result);
    }

    // Listar por tarjeta (requiere que la tarjeta sea del cliente autenticado)
    @GetMapping("/api/withdraws/card/{cardId}")
    public ResponseEntity<?> listByCard(@PathVariable Integer cardId, HttpServletRequest request) {
        // (Puedes reutilizar misma verificación de propiedad que en create)
        return ResponseEntity.ok(withdrawService.findByCard(cardId));
    }

    // Listar por cuenta (útil si quieres ver todos los withdraws de todas sus tarjetas)
    @GetMapping("/api/withdraws/account/{accountId}")
    public ResponseEntity<?> listByAccount(@PathVariable Integer accountId) {
        return ResponseEntity.ok(withdrawService.findByAccount(accountId));
    }

    @DeleteMapping("/api/withdraws/delete/card/{cardId}")
    public ResponseEntity<String> deleteWithdrawsById(@PathVariable int cardId) {
        // Check if card exists
        Optional<Card> card = cardService.findById(cardId);
        if (card.isEmpty())
            return ResponseEntity.badRequest().body("Error: customer does not exist");

        // We delete every withdraw of the card
        List<Withdraw> withdraws = withdrawService.findByCard(cardId);
        for (Withdraw withdraw : withdraws){
            this.withdrawService.deleteById(withdraw.getWithdrawId());
        }
        return ResponseEntity.ok("Customer cards deleted successfully");
    }
}
