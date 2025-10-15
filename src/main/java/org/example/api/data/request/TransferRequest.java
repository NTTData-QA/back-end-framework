package org.example.api.data.request;

import jakarta.persistence.*;
import org.example.api.data.entity.Transfer;
import lombok.Data;

@Data
public class TransferRequest {

    @Column(nullable = false)
    private Double transferAmount;
    private Integer originAccountId;  // 1 originAccount - N transfers
    private Integer receivingAccountId;  // 1 receivingAccount - N transfers
    private Transfer.CurrencyType currencyType;


}
