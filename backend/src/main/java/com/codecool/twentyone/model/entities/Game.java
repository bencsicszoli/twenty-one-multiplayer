package com.codecool.twentyone.model.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gameId;
    private String player1;
    private String player2;
    private String player3;
    private int player1Balance;
    private int player2Balance;
    private int player3Balance;
    private int dealerBalance;

}
