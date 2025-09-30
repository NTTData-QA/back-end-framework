package org.example.api.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.security.core.userdetails.User;


@Data
@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer customerId;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String surname;

    @Column(unique = true, nullable = false)
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Email debe ser válido")
    private String email;
    @NotBlank(message = "La contraseña es obligatoria")
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UserType role;

    public enum UserType {
        ADMIN,          // Administrator
        USER,           // Default Customer
        // Could add minor user, not letting them do transfers and needing a grown-up user as co-owner of account (Account need List as customerIds)
    }

    @JsonIgnore
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)    // si se elimina un cliente se eliminan sus cuentas
    private List<Account> accounts;

    @JsonProperty(value = "accountIds", access = JsonProperty.Access.READ_ONLY)
    public List<Integer> getAccountIds() {
        return accounts == null
                ? Collections.emptyList()
                : accounts.stream().map(Account::getAccountId).collect(Collectors.toList());
    }

    public boolean deleteAccount(int accountId){
        int i = 0;
        boolean found = false;

        while(i < accounts.size() && !found){
            found = accounts.get(i).getAccountId() == accountId;
            if (!found) i++;
        }

        if (found) accounts.remove(i);

        return found;
    }

    public void deleteAllAccounts(){
        accounts.clear();
    }
}
