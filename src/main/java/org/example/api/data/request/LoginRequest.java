package org.example.api.data.request;

import jakarta.persistence.*;
import lombok.Data;

@Data
public class LoginRequest {
    private Integer id;
    private String email;
    private String password;
}
