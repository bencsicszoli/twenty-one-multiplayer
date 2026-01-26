package com.codecool.twentyone.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "player_hand")
public class PlayerCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int cardValue;
    private String frontImagePath;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;
}
