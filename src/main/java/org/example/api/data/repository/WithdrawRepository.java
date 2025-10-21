package org.example.api.data.repository;

import org.example.api.data.entity.Withdraw;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WithdrawRepository extends JpaRepository<Withdraw, Integer> {
  List<Withdraw> findByCard_CardId(Integer cardId);
  List<Withdraw> findByCard_Account_AccountId(Integer accountId);
  List<Withdraw> findByCard_Account_Customer_CustomerId(Integer customerId);
}
