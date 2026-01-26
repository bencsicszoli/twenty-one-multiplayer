package com.codecool.twentyone.service;

import com.codecool.twentyone.model.dto.websocketdto.CardDTO;
import com.codecool.twentyone.model.dto.GameMessage;
import com.codecool.twentyone.model.dto.websocketdto.PublicHandDTO;
import com.codecool.twentyone.model.entities.*;
import com.codecool.twentyone.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private final GameRepository gameRepository;
    private final DealerRepository dealerRepository;
    private final PlayerRepository playerRepository;
    private final PlayerHandRepository playerHandRepository;

    public MessageService(GameRepository gameRepository, DealerRepository dealerRepository, PlayerRepository playerRepository, PlayerHandRepository playerHandRepository) {
        this.gameRepository = gameRepository;
        this.dealerRepository = dealerRepository;
        this.playerRepository = playerRepository;
        this.playerHandRepository = playerHandRepository;
    }

    public synchronized Game joinGame(String player) {
        Optional<Game> game = gameRepository.findFirstGameByPlayer(player);
        if (game.isPresent()) {
            return game.get();
        }
        Optional<Game> existingGameOpt = gameRepository.findFirstGameByMissingPlayer();
        if (existingGameOpt.isPresent()) {
            Game existingGame = existingGameOpt.get();
            if (existingGame.getPlayer1() == null) {
                existingGame.setPlayer1(player);
                existingGame.setPlayer1Balance(playerRepository.getBalanceByPlayerName(player));
                return gameRepository.save(existingGame);
            } else if (existingGame.getPlayer2() == null) {
                existingGame.setPlayer2(player);
                existingGame.setPlayer2Balance(playerRepository.getBalanceByPlayerName(player));
                return gameRepository.save(existingGame);
            } else if (existingGame.getPlayer3() == null) {
                existingGame.setPlayer3(player);
                existingGame.setPlayer3Balance(playerRepository.getBalanceByPlayerName(player));
                return gameRepository.save(existingGame);
            } else {
                existingGame.setPlayer4(player);
                existingGame.setPlayer4Balance(playerRepository.getBalanceByPlayerName(player));
                return gameRepository.save(existingGame);
            }
        }
        Game newGame = new Game();
        Dealer dealer = dealerRepository.save(new Dealer());
        newGame.setDealerId(dealer.getId());
        newGame.setPlayer1(player);
        newGame.setPlayer2(null);
        newGame.setPlayer3(null);
        newGame.setPlayer4(null);
        newGame.setPlayer1Balance(playerRepository.getBalanceByPlayerName(player));
        newGame.setPlayer2Balance(0);
        newGame.setPlayer3Balance(0);
        newGame.setPlayer4Balance(0);
        newGame.setDealerBalance(100);
        newGame.setTurnName(player);
        return gameRepository.save(newGame);
    }

    public GameMessage gameToMessage(Game game) {
        GameMessage message = new GameMessage();
        message.setGameId(game.getGameId());
        message.setPlayer1(game.getPlayer1());
        message.setPlayer2(game.getPlayer2());
        message.setPlayer3(game.getPlayer3());
        message.setPlayer4(game.getPlayer4());
        message.setPlayer1Balance(game.getPlayer1Balance());
        message.setPlayer2Balance(game.getPlayer2Balance());
        message.setPlayer3Balance(game.getPlayer3Balance());
        message.setPlayer4Balance(game.getPlayer4Balance());
        message.setDealerBalance(game.getDealerBalance());
        message.setTurnName(game.getTurnName());
        message.setRemainingCards(game.getRemainingCards());
        message.setState(game.getState().toString());
        message.setContent(game.getInformation());
        message.setLastCard(game.isLastCard());
        PublicHandDTO player1PublicHand = new PublicHandDTO(List.of(), 0);
        PublicHandDTO player2PublicHand = new PublicHandDTO(List.of(), 0);
        PublicHandDTO player3PublicHand = new PublicHandDTO(List.of(), 0);
        PublicHandDTO player4PublicHand = new PublicHandDTO(List.of(), 0);

        String player1Name = game.getPlayer1();
        String player2Name = game.getPlayer2();
        String player3Name = game.getPlayer3();
        String player4Name = game.getPlayer4();
        int player1CardNumber = 0;
        int player2CardNumber = 0;
        int player3CardNumber = 0;
        int player4CardNumber = 0;
        int player1Pot = 0;
        int player2Pot = 0;
        int player3Pot = 0;
        int player4Pot = 0;

        if (player1Name != null) {
            Player player = playerRepository.findByPlayerName(player1Name).orElseThrow(()-> new RuntimeException("Player " + player1Name + " not found"));
            player1CardNumber = player.getCardNumber();
            player1Pot = player.getPot();
            if (game.isPublicHand1Exists()) {
                List<PlayerCard> playerCard = playerHandRepository.findAllByPlayerId(player.getId()).orElseThrow(() -> new RuntimeException("Player 1 hand not found"));
                player1PublicHand = getPublicHandDTO(playerCard);
            }
        }
        message.setPlayer1PublicHand(player1PublicHand);

        if (player2Name != null) {
            Player player = playerRepository.findByPlayerName(player2Name).orElseThrow(()-> new RuntimeException("Player " + player2Name + " not found"));
            player2CardNumber = player.getCardNumber();
            player2Pot = player.getPot();
            if (game.isPublicHand2Exists()) {
                List<PlayerCard> playerCard = playerHandRepository.findAllByPlayerId(player.getId()).orElseThrow(() -> new RuntimeException("Player 2 hand not found"));
                player2PublicHand = getPublicHandDTO(playerCard);
            }
        }
        message.setPlayer2PublicHand(player2PublicHand);

        if (player3Name != null) {
            Player player = playerRepository.findByPlayerName(player3Name).orElseThrow(()-> new RuntimeException("Player " + player3Name + " not found"));
            player3CardNumber = player.getCardNumber();
            player3Pot = player.getPot();
            if (game.isPublicHand3Exists()) {
                List<PlayerCard> playerCard = playerHandRepository.findAllByPlayerId(player.getId()).orElseThrow(() -> new RuntimeException("Player 3 hand not found"));
                player3PublicHand = getPublicHandDTO(playerCard);
            }
        }
        message.setPlayer3PublicHand(player3PublicHand);

        if (player4Name != null) {
            Player player = playerRepository.findByPlayerName(player4Name).orElseThrow(()-> new RuntimeException("Player " + player4Name + " not found"));
            player4CardNumber = player.getCardNumber();
            player4Pot = player.getPot();
            if (game.isPublicHand4Exists()) {
                List<PlayerCard> playerCard = playerHandRepository.findAllByPlayerId(player.getId()).orElseThrow(() -> new RuntimeException("Player 4 hand not found"));
                player4PublicHand = getPublicHandDTO(playerCard);
            }
        }
        message.setPlayer4PublicHand(player4PublicHand);

        Dealer dealer = dealerRepository.findById(game.getDealerId()).orElseThrow(()-> new RuntimeException("Dealer not found"));
        int dealerCardNumber = dealer.getCardNumber();

        message.setPlayer1CardNumber(player1CardNumber);
        message.setPlayer2CardNumber(player2CardNumber);
        message.setPlayer3CardNumber(player3CardNumber);
        message.setPlayer4CardNumber(player4CardNumber);
        message.setDealerCardNumber(dealerCardNumber);
        message.setPlayer1Pot(player1Pot);
        message.setPlayer2Pot(player2Pot);
        message.setPlayer3Pot(player3Pot);
        message.setPlayer4Pot(player4Pot);
        message.setDealerBalance(dealer.getBalance());

        return message;
    }

    private PublicHandDTO getPublicHandDTO(List<PlayerCard> playerCard) {
        PublicHandDTO playerPublicHand;
        List<CardDTO> dtoList = new ArrayList<>();
        int handValue = 0;
        for (PlayerCard card : playerCard) {
            CardDTO dto = new CardDTO(card.getCardValue(), card.getFrontImagePath());
            dtoList.add(dto);
            handValue += card.getCardValue();
        }
        playerPublicHand = new PublicHandDTO(dtoList, handValue);
        return playerPublicHand;
    }
}
