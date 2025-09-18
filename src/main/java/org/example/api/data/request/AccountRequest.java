package org.example.api.data.request;

import lombok.Data;
import org.example.api.data.entity.Account;


@Data
public class AccountRequest {
    private Account.AccountType accountType;
    private Boolean isBlocked;
}
