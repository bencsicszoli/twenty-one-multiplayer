package com.codecool.twentyone.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "dealer_hand")
public class DealerCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int cardValue;
    private String frontImagePath;

    @ManyToOne
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;
}
