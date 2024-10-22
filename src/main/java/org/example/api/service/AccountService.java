package org.example.api.service;

import org.example.api.data.entity.Account;
import org.example.api.data.entity.Card;
import org.example.api.data.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
  @Autowired private CardService cardService;
  @Autowired private AccountRepository accountRepository;

  public List<Account> findAll() {
    return accountRepository.findAll();
  }

  public Optional<Account> findById(Integer accountId) {
    return accountRepository.findById(accountId);
  }

  public Account save(Account account) {
    return accountRepository.save(account);
  }

  public void deleteById(Integer accountId) {
    accountRepository.deleteById(accountId);
  }

  public List<Account> findByCustomer(Integer customerId) {return accountRepository.findByCustomer_CustomerId(customerId);}

  public boolean checkAccountInDebt(Account account){
    return account.getAmount() < 0;
  }

  public void makeDeposit(Account account, Double deposit){
    Double accountAmount = account.getAmount();
    account.setAmount(accountAmount + deposit);
    accountRepository.save(account);
  }

  public void makeWithdraw(Account account, Double withdraw){
    Double accountAmount = account.getAmount();
    account.setAmount(accountAmount - withdraw);
    accountRepository.save(account);
  }


}
