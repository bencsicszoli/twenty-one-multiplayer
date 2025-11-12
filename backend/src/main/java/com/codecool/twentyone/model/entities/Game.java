package com.codecool.twentyone.model.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gameId;
    private String player1;
    private String player2;
    private String player3;
    private Long dealerId;
    private int player1Balance;
    private int player2Balance;
    private int player3Balance;
    private int dealerBalance;
    private int remainingCards = 32;
    private String turnName;

    @Enumerated(EnumType.STRING)
    @Column(name="game_state")
    private GameState state = GameState.NEW;
}
