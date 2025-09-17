package org.example.api.service;

import org.example.api.data.entity.Account;
import org.example.api.data.entity.Card;
import org.example.api.data.entity.Withdraw;
import org.example.api.data.repository.AccountRepository;
import org.example.api.data.repository.WithdrawRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class WithdrawService {

    private final WithdrawRepository withdrawRepository;

    private final AccountRepository accountRepository;

    public WithdrawService(WithdrawRepository withdrawRepository,
                           AccountRepository accountRepository) {
        this.withdrawRepository = withdrawRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Withdraw createWithdraw(Card card, Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be > 0");
        }

        Account account = card.getAccount();
        Double balance = account.getAmount();

        if (account.getIsInDebt() == Boolean.TRUE){
            throw new IllegalArgumentException("Account is in debt");
        }

        if (balance < amount) {
            throw new IllegalStateException("Insufficient funds");
        }

        if (card.getIsBlocked() == Boolean.TRUE){
            throw new IllegalArgumentException("Card is blocked");
        }

        if (account.getIsBlocked() == Boolean.TRUE){
            throw new IllegalArgumentException("Account is blocked");
        }

        // 1) Actualiza saldo de la cuenta
        int cardId = card.getCardId();
        List<Withdraw> withdrawsList = findByCard(cardId);
        //fechas actuales dia/ mes / año y guardarlo en variables
        LocalDateTime now = LocalDateTime.now();
        int day = now.getDayOfMonth();
        int month = now.getMonthValue();
        int year = now.getYear();
        Double dailyLimit = 0.;
        Double monthlyLimit = 0.;

        for(Withdraw withdraw: withdrawsList){
            LocalDateTime fecha = withdraw.getWithdrawDate();
            if(day == fecha.getDayOfMonth() && month == fecha.getMonthValue() && year == fecha.getYear()){
                dailyLimit += withdraw.getAmount();
            }

            if(month == fecha.getMonthValue() && year == fecha.getYear()){
                monthlyLimit += withdraw.getAmount();
            }
        }
        if( (dailyLimit + amount) > card.getDailyLimit()){
            Double saldoDisponible = card.getDailyLimit() - dailyLimit;
            throw new IllegalArgumentException("Saldo diario insuficiente, saldo restante diario: " +saldoDisponible + "dailyLimit "+ dailyLimit);
        }
        if( (monthlyLimit + amount) > card.getMonthlyLimit()){
            Double saldoDisponible1 = card.getMonthlyLimit() - monthlyLimit;
            throw new IllegalArgumentException("Saldo mensual insuficiente, saldo restante mensual: " +saldoDisponible1);
        }

        account.setAmount(balance - amount);
        accountRepository.save(account);

        // 2) Guarda el withdraw
        Withdraw w = new Withdraw();
        w.setCard(card);
        w.setAmount(amount);
        w.setWithdrawDate(now);
        return withdrawRepository.save(w);
    }

    public List<Withdraw> findByCard(Integer cardId) {
        return withdrawRepository.findByCard_CardId(cardId);
    }
    public List<Withdraw> findByAccount(Integer accountId) {
        return withdrawRepository.findByCard_Account_AccountId(accountId);
    }
    public List<Withdraw> findByCustomer(Integer customerId) {
        return withdrawRepository.findByCard_Account_Customer_CustomerId(customerId);
    }
}
