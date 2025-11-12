package com.codecool.twentyone.service;

import com.codecool.twentyone.model.dto.GameMessage;
import com.codecool.twentyone.model.entities.Dealer;
import com.codecool.twentyone.model.entities.Game;
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
                return gameRepository.save(existingGame);
            } else if (existingGame.getPlayer2() == null) {
                existingGame.setPlayer2(player);
                return gameRepository.save(existingGame);
            } else {
                existingGame.setPlayer3(player);
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
        if (player1Name != null) {
            player1CardNumber = playerRepository.cardNumberByPlayerName(player1Name);
        }
        if (player2Name != null) {
            player2CardNumber = playerRepository.cardNumberByPlayerName(player2Name);
        }
        if (player3Name != null) {
            player3CardNumber = playerRepository.cardNumberByPlayerName(player3Name);
        }

        message.setPlayer1CardNumber(player1CardNumber);
        message.setPlayer2CardNumber(player2CardNumber);
        message.setPlayer3CardNumber(player3CardNumber);

        return message;
    }
}
