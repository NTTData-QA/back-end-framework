package org.example.api.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer accountId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    private Boolean isBlocked;
    private Boolean isInDebt;
    private Double amount;
    private LocalDateTime creationDate;
    private LocalDateTime expirationDate;

    public enum AccountType {
        CHECKING_ACCOUNT,       // Checking Account
        SAVINGS_ACCOUNT,         // Savings Account
        BUSINESS_ACCOUNT,     // Business Account
        CHILDREN_ACCOUNT
    }

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "customerId", nullable = false)
    private Customer customer;  // N accounts - 1 customer .

    @JsonIgnore
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Card> cards;    // 1 account - N cards

    @JsonIgnore
    @OneToMany(mappedBy = "originAccount")
    private List<Transfer> originatingTransfers;    // 1 account - N originating transfers

    @JsonIgnore
    @OneToMany(mappedBy = "receivingAccount")
    private List<Transfer> receivingTransfers;      // 1 account - N receiving transfers

    public boolean deleteCard(int cardId){
        int i = 0;
        boolean found = false;

        while (i < cards.size() && !found){
            found = cards.get(i).getCardId() == cardId;
            if (!found) i++;
        }
        if (found) cards.remove(i);

        return found;
    }

    public void deleteAllCards(){
        cards.clear();
    }
}
