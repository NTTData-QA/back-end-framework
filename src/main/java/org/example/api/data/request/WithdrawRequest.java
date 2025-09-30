package org.example.api.data.request;

import lombok.Data;
import java.util.Date;

@Data
public class WithdrawRequest {
    private Double amount;      // cantidad a retirar
    private Integer cardId;     // tarjeta con la que se hace
    private Date withdrawDate;  // opcional; si viene null, el backend pone "ahora"
}
