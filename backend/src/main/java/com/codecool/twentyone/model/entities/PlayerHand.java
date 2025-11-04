package com.codecool.twentyone.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PlayerHand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String cardName;
    private String cardColor;
    private int cardValue;
    private String frontImagePath;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;
}
