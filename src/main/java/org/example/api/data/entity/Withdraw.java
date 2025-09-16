package org.example.api.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
public class Withdraw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer withdrawId;

    @Column(nullable = false)
    private Double amount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date withdrawDate = new Date(); // por defecto, ahora

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;
}
