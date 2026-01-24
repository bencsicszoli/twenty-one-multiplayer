package com.codecool.twentyone.model.entities;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum PlayerSlot {

    PLAYER1(
            Game::getPlayer1,
            Game::setPlayer1,
            Game::setPlayer1Balance,
            Game::setPublicHand1Exists,
            "player1"
    ),
    PLAYER2(
            Game::getPlayer2,
            Game::setPlayer2,
            Game::setPlayer2Balance,
            Game::setPublicHand2Exists,
            "player2"
    ),
    PLAYER3(
            Game::getPlayer3,
            Game::setPlayer3,
            Game::setPlayer3Balance,
            Game::setPublicHand3Exists,
            "player3"
    ),
    PLAYER4(
            Game::getPlayer4,
            Game::setPlayer4,
            Game::setPlayer4Balance,
            Game::setPublicHand4Exists,
            "player4"
    );

    public final Function<Game, String> getPlayer;
    public final BiConsumer<Game, String> setPlayer;
    public final BiConsumer<Game, Integer> setBalance;
    public final BiConsumer<Game, Boolean> setPublicHandExists;
    public final String playerName;

    PlayerSlot(
            Function<Game, String> getPlayer,
            BiConsumer<Game, String> setPlayer,
            BiConsumer<Game, Integer> setBalance,
            BiConsumer<Game, Boolean> setPublicHandExists,
            String playerName
    ) {
        this.getPlayer = getPlayer;
        this.setPlayer = setPlayer;
        this.setBalance = setBalance;
        this.setPublicHandExists = setPublicHandExists;
        this.playerName = playerName;
    }
}

