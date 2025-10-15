package org.example.api.data.request;

import lombok.Data;
import org.example.api.data.entity.Card;

@Data
public class CardRequest {
    private Card.CardType type;
    private int accountId;
}
