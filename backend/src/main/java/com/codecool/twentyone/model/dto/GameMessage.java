package com.codecool.twentyone.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class GameMessage {
    private Long gameId;
    private String player1;
    private String player2;
    private String player3;
    private int player1Balance;
    private int player1CardNumber;
    private int player1Pot;
    private int player2Balance;
    private int player2CardNumber;
    private int player2Pot;
    private int player3Balance;
    private int player3CardNumber;
    private int player3Pot;
    private int dealerBalance;
    private int dealerCardNumber;
    private int remainingCards;
    private String type;
    private String senderName;
    private String turnName;
    private String content;
    private String state;
}
