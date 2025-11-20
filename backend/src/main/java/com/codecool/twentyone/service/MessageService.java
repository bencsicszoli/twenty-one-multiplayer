package com.codecool.twentyone.service;

import com.codecool.twentyone.model.dto.GameMessage;
import com.codecool.twentyone.model.entities.Dealer;
import com.codecool.twentyone.model.entities.Game;
import com.codecool.twentyone.model.entities.Player;
import com.codecool.twentyone.repository.DealerRepository;
import com.codecool.twentyone.repository.GameRepository;
import com.codecool.twentyone.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MessageService {

    private final GameRepository gameRepository;
    private final DealerRepository dealerRepository;
    private final PlayerRepository playerRepository;

    public MessageService(GameRepository gameRepository, DealerRepository dealerRepository, PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.dealerRepository = dealerRepository;
        this.playerRepository = playerRepository;
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
                existingGame.setPlayer2Balance(100);
                return gameRepository.save(existingGame);
            } else if (existingGame.getPlayer2() == null) {
                existingGame.setPlayer2(player);
                existingGame.setPlayer2Balance(100);
                return gameRepository.save(existingGame);
            } else {
                existingGame.setPlayer3(player);
                existingGame.setPlayer3Balance(100);
                return gameRepository.save(existingGame);
            }
        }
        Game newGame = new Game();
        Dealer dealer = dealerRepository.save(new Dealer());
        newGame.setDealerId(dealer.getId());
        newGame.setPlayer1(player);
        newGame.setPlayer2(null);
        newGame.setPlayer3(null);
        newGame.setPlayer1Balance(100);
        newGame.setPlayer2Balance(0);
        newGame.setPlayer3Balance(0);
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
        message.setPlayer1Balance(game.getPlayer1Balance());
        message.setPlayer2Balance(game.getPlayer2Balance());
        message.setPlayer3Balance(game.getPlayer3Balance());
        message.setDealerBalance(game.getDealerBalance());
        message.setTurnName(game.getTurnName());
        message.setRemainingCards(game.getRemainingCards());
        message.setState(game.getState().toString());
        String player1Name = game.getPlayer1();
        String player2Name = game.getPlayer2();
        String player3Name = game.getPlayer3();
        int player1CardNumber = 0;
        int player2CardNumber = 0;
        int player3CardNumber = 0;
        int player1Pot = 0;
        int player2Pot = 0;
        int player3Pot = 0;
        if (player1Name != null) {
            Player player1 = playerRepository.findByPlayerName(player1Name).orElseThrow(()-> new RuntimeException("Player " + player1Name + " not found"));
            player1CardNumber = player1.getCardNumber();
            player1Pot = player1.getPot();
        }
        if (player2Name != null) {
            Player player2 = playerRepository.findByPlayerName(player2Name).orElseThrow();
            player2CardNumber = player2.getCardNumber();
            player2Pot = player2.getPot();
        }
        if (player3Name != null) {
            Player player3 = playerRepository.findByPlayerName(player3Name).orElseThrow();
            player3CardNumber = player3.getCardNumber();
            player3Pot = player3.getPot();
        }
        int dealerCardNumber = dealerRepository.findCardNumberById(game.getDealerId());

        message.setPlayer1CardNumber(player1CardNumber);
        message.setPlayer2CardNumber(player2CardNumber);
        message.setPlayer3CardNumber(player3CardNumber);
        message.setDealerCardNumber(dealerCardNumber);
        message.setPlayer1Pot(player1Pot);
        message.setPlayer2Pot(player2Pot);
        message.setPlayer3Pot(player3Pot);

        return message;
    }
}
