package org.example.api.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;

@Data
@Entity
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cardId;

    @Column(unique = true, nullable = false)
    private Long number;
    @Column(nullable = false)
    private int cvc;
    @Column(nullable = false)
    private Date expirationDate;
    private Boolean isBlocked;
    private Double dailyLimit;
    private Double monthlyLimit;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private CardType type;

    public enum CardType {
        CREDIT,
        DEBIT,
    }

}
