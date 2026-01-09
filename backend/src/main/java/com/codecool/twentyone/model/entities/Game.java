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
    private String player4;
    private Long dealerId;
    private int player1Balance;
    private int player2Balance;
    private int player3Balance;
    private int player4Balance;
    private int dealerBalance;
    private int remainingCards = 32;
    private String turnName;
    private String information = "";
    private boolean publicHand1Exists = false;
    private boolean publicHand2Exists = false;
    private boolean publicHand3Exists = false;
    private boolean publicHand4Exists = false;
    private int cardOrder = 1;
    private boolean lastCard = false;

    @Enumerated(EnumType.STRING)
    @Column(name="game_state")
    private GameState state = GameState.NEW;
}
