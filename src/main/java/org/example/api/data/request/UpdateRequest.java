package org.example.api.data.request;

import lombok.Data;
@Data
public class UpdateRequest {
    private String name;
    private String surname;
    private String email;
    private String password;
    private Double Amount;
    private Double DailyLimit;
    private Double MonthlyLimit;
}
