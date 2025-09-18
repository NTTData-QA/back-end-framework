package org.example.api.service;

import org.example.api.data.entity.Account;
import org.example.api.data.entity.Card;
import org.example.api.data.repository.CardRepository;
import org.example.api.data.request.UpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    public List<Card> findAll() {
        return cardRepository.findAll();
    }

    public Optional<Card> findById(Integer cardId) {
        return cardRepository.findByCardId(cardId);
    }

    public Card save(Card card) {
        return cardRepository.save(card);
    }

    public void deleteById(Integer cardId) {
        cardRepository.deleteById(cardId);
    }

    public List<Card> findByAccountId(Integer accountId) {
        return cardRepository.findByAccount_AccountId(accountId);
    }

    public List<Card> getCardsByCustomerId(Integer customerId) {
        return cardRepository.findByAccount_Customer_CustomerId(customerId);
    }
    @Transactional
    public void deleteCardsByAccount(Integer accountId){
        cardRepository.deleteByAccount_AccountId(accountId);
    }

    public Card convertCardToEntity(Card card, Account account){
        Card newCard = new Card();
        newCard.setCardId(card.getCardId());
        newCard.setType(card.getType());
        newCard.setAccount(account);
        newCard.setNumber(card.getNumber());
        newCard.setCvc(card.getCvc());
        newCard.setExpirationDate(card.getExpirationDate());
        newCard.setIsBlocked((card.getIsBlocked()));
        newCard.setDailyLimit(card.getDailyLimit());
        newCard.setMonthlyLimit(card.getMonthlyLimit());

        return newCard;
    }

    public void updateDailyLimit(Card card, Double newDailyLimit){
        card.setDailyLimit(newDailyLimit);
        cardRepository.save(card);
    }

    public void updateMonthlyLimit(Card card, Double newMonthlyLimit){
        card.setMonthlyLimit(newMonthlyLimit);
        cardRepository.save(card);
    }

}
