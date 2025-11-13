package com.codecool.twentyone.service;

import com.codecool.twentyone.model.dto.CardDTO;
import com.codecool.twentyone.model.dto.GameMessage;
import com.codecool.twentyone.model.dto.HandDTO;
import com.codecool.twentyone.model.entities.*;
import com.codecool.twentyone.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final PlayerHandRepository playerHandRepository;
    private final DealerHandRepository dealerHandRepository;
    private final DealerRepository dealerRepository;
    private final ShuffleRepository shuffleRepository;
    private static int cardOrder = 1;
    private final ShuffleService shuffleService;
    private final MessageService messageService;

    public GameService(GameRepository gameRepository, PlayerRepository playerRepository, PlayerHandRepository playerHandRepository, DealerHandRepository dealerHandRepository, DealerRepository dealerRepository, ShuffleRepository shuffleRepository, ShuffleService shuffleService, MessageService messageService) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.playerHandRepository = playerHandRepository;
        this.dealerHandRepository = dealerHandRepository;
        this.dealerRepository = dealerRepository;
        this.shuffleRepository = shuffleRepository;
        this.shuffleService = shuffleService;
        this.messageService = messageService;
    }

    public HandDTO getFirstCard(Long gameId, String playerName) {
        shuffleService.addShuffledDeck(gameId);
        Card firstCard = shuffleRepository.findCardByGameIdAndCardOrder(gameId, cardOrder).orElseThrow(() -> new RuntimeException("Card not found"));
        Player player = playerRepository.findByPlayerName(playerName).orElseThrow(() -> new RuntimeException("Player not found"));
        PlayerHand hand = new PlayerHand();
        hand.setCardValue(firstCard.getValue());
        hand.setFrontImagePath(firstCard.getFrontImagePath());
        hand.setPlayer(player);
        playerHandRepository.save(hand);
        int handValue = playerHandRepository.getHandValue(hand.getPlayer().getId());
        System.out.println("Hand value: " + handValue);
        CardDTO dto = new CardDTO(firstCard.getValue(), firstCard.getFrontImagePath());
        return new HandDTO(PlayerState.WAITING_CARD, List.of(dto), handValue, "hand.update");
    }

    public GameMessage pullCard(Long gameId, String playerName) {
        Player currentPlayer = playerRepository.findByPlayerName(playerName).orElseThrow(() -> new RuntimeException("Player not found"));
        currentPlayer.setCardNumber(currentPlayer.getCardNumber() + 1);
        playerRepository.save(currentPlayer);
        cardOrder++;
        Game currentGame = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        currentGame.setRemainingCards(currentGame.getRemainingCards() - 1);
        if (currentGame.getState().equals(GameState.NEW)) {
            currentGame.setState(GameState.IN_PROGRESS);
        }
        Game updatedGame = gameRepository.save(currentGame);
        return messageService.gameToMessage(updatedGame);
    }

    public GameMessage passTurn(Long gameId, String turnPlayerName) {
        Game currentGame = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        String[] players = {currentGame.getPlayer1(), currentGame.getPlayer2(), currentGame.getPlayer3(), currentGame.getPlayer1(), currentGame.getPlayer2()};

        for (int i = 0; i < 3; i++) {
            if (players[i].equals(turnPlayerName) && players[i + 1] != null) {
                Player player = playerRepository.findByPlayerName(players[i + 1]).orElseThrow(() -> new RuntimeException("Player not found"));
                if (player.getPlayerState().equals(PlayerState.WAITING_CARD)) {
                    currentGame.setTurnName(player.getPlayerName());
                    gameRepository.save(currentGame);
                    return messageService.gameToMessage(currentGame);
                }
            } else if (players[i].equals(turnPlayerName) && players[i + 2] != null) {
                Player player = playerRepository.findByPlayerName(players[i + 2]).orElseThrow(() -> new RuntimeException("Player not found"));
                if (player.getPlayerState().equals(PlayerState.WAITING_CARD)) {
                    currentGame.setTurnName(player.getPlayerName());
                    gameRepository.save(currentGame);
                    return messageService.gameToMessage(currentGame);
                }
            } else if (players[i].equals(turnPlayerName)) {
                Player player = playerRepository.findByPlayerName(turnPlayerName).orElseThrow(() -> new RuntimeException("Player not found"));
                if (player.getPlayerState().equals(PlayerState.WAITING_CARD)) {
                    return messageService.gameToMessage(currentGame);
                }
                currentGame.setTurnName("Dealer");
                gameRepository.save(currentGame);
                return messageService.gameToMessage(currentGame);
            }
        }
        return null;
    }
}
