package com.codecool.twentyone.service;

import com.codecool.twentyone.model.dto.GameMessage;
import com.codecool.twentyone.model.dto.websocketdto.CardDTO;
import com.codecool.twentyone.model.dto.websocketdto.PublicHandDTO;
import com.codecool.twentyone.model.entities.*;
import com.codecool.twentyone.repository.DealerRepository;
import com.codecool.twentyone.repository.GameRepository;
import com.codecool.twentyone.repository.PlayerHandRepository;
import com.codecool.twentyone.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock
    GameRepository gameRepository;

    @Mock
    PlayerRepository playerRepository;

    @Mock
    PlayerHandRepository playerHandRepository;

    @Mock
    DealerRepository dealerRepository;

    @InjectMocks
    private MessageService messageService;

    @Test
    void joinGame_shouldReturnNewGame_when_thereIsNoSeatAvailable() {
        String playerName = "john";
        when(gameRepository.findFirstGameByPlayer(playerName)).thenReturn(Optional.empty());
        when(gameRepository.findFirstGameByMissingPlayer()).thenReturn(Optional.empty());
        Dealer dealer = new Dealer();
        when(dealerRepository.save(any(Dealer.class))).thenReturn(dealer);
        when(playerRepository.getBalanceByPlayerName(playerName)).thenReturn(90);

        messageService.joinGame(playerName);

        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(gameCaptor.capture());
        Game capturedGame = gameCaptor.getValue();
        assertEquals("john", capturedGame.getPlayer1());
        assertNull(capturedGame.getPlayer3());
        assertEquals(90, capturedGame.getPlayer1Balance());
        assertEquals("john", capturedGame.getTurnName());
    }

    @Test
    void joinGame_shouldReturnGame_when_gameExistsWithEmptySeatAtFirstPlace() {
        String playerName = "john";
        when(gameRepository.findFirstGameByPlayer(playerName)).thenReturn(Optional.empty());
        Game game = new Game();
        game.setPlayer2("Jane");
        when(gameRepository.findFirstGameByMissingPlayer()).thenReturn(Optional.of(game));
        when(playerRepository.getBalanceByPlayerName(playerName)).thenReturn(90);

        messageService.joinGame(playerName);

        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(gameCaptor.capture());
        Game capturedGame = gameCaptor.getValue();
        assertEquals("john", capturedGame.getPlayer1());
        assertEquals(90, capturedGame.getPlayer1Balance());
    }

    @Test
    void joinGame_shouldReturnGame_when_gameExistsWithEmptySeatAtSecondPlace() {
        String playerName = "john";
        when(gameRepository.findFirstGameByPlayer(playerName)).thenReturn(Optional.empty());
        Game game = new Game();
        game.setPlayer1("Jane");
        when(gameRepository.findFirstGameByMissingPlayer()).thenReturn(Optional.of(game));
        when(playerRepository.getBalanceByPlayerName(playerName)).thenReturn(80);

        messageService.joinGame(playerName);

        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(gameCaptor.capture());
        Game capturedGame = gameCaptor.getValue();
        assertEquals("john", capturedGame.getPlayer2());
        assertEquals(80, capturedGame.getPlayer2Balance());
    }

    @Test
    void joinGame_shouldReturnGame_when_gameExistsWithEmptySeatAtThirdPlace() {
        String playerName = "john";
        when(gameRepository.findFirstGameByPlayer(playerName)).thenReturn(Optional.empty());
        Game game = new Game();
        game.setPlayer1("Jane");
        game.setPlayer2("Jack");
        when(gameRepository.findFirstGameByMissingPlayer()).thenReturn(Optional.of(game));
        when(playerRepository.getBalanceByPlayerName(playerName)).thenReturn(70);

        messageService.joinGame(playerName);

        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(gameCaptor.capture());
        Game capturedGame = gameCaptor.getValue();
        assertEquals("john", capturedGame.getPlayer3());
        assertEquals(70, capturedGame.getPlayer3Balance());
    }

    @Test
    void joinGame_shouldReturnGame_when_gameExistsWithEmptySeatAtFourthPlace() {
        String playerName = "john";
        when(gameRepository.findFirstGameByPlayer(playerName)).thenReturn(Optional.empty());
        Game game = new Game();
        game.setPlayer1("Jane");
        game.setPlayer2("Jack");
        game.setPlayer3("Jill");
        when(gameRepository.findFirstGameByMissingPlayer()).thenReturn(Optional.of(game));
        when(playerRepository.getBalanceByPlayerName(playerName)).thenReturn(60);

        messageService.joinGame(playerName);

        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(gameCaptor.capture());
        Game capturedGame = gameCaptor.getValue();
        assertEquals("john", capturedGame.getPlayer4());
        assertEquals(60, capturedGame.getPlayer4Balance());
    }

    @Test
    void joinGame_shouldReturnGame_when_playerIsAlreadyInGame() {
        String playerName = "john";
        Game game = new Game();
        game.setPlayer1("john");
        game.setPlayer1Balance(73);
        when(gameRepository.findFirstGameByPlayer(playerName)).thenReturn(Optional.of(game));

        Game result = messageService.joinGame(playerName);

        assertEquals("john", result.getPlayer1());
        assertEquals(73, result.getPlayer1Balance());
    }

    @Test
    void gameToMessage_shouldReturnGameMessage_when_player1AndPlayer3havePublicHands() {
        Game game = getGame();
        Player player1 = new Player();
        player1.setId(2L);
        player1.setPlayerName("Jane");
        player1.setCardNumber(3);
        player1.setPot(0);
        when(playerRepository.findByPlayerName(player1.getPlayerName())).thenReturn(Optional.of(player1));
        PlayerCard playerCard1 = new PlayerCard();
        playerCard1.setCardValue(7);
        playerCard1.setFrontImagePath("card1.png");
        PlayerCard playerCard2 = new PlayerCard();
        playerCard2.setCardValue(7);
        playerCard2.setFrontImagePath("card2.png");
        PlayerCard playerCard3 = new PlayerCard();
        playerCard3.setCardValue(9);
        playerCard3.setFrontImagePath("card3.png");
        when(playerHandRepository.findAllByPlayerId(2L)).thenReturn(Optional.of(List.of(playerCard1, playerCard2, playerCard3)));
        Player player2 = new Player();
        player2.setId(3L);
        player2.setPlayerName("Jack");
        player2.setCardNumber(2);
        player2.setPot(4);
        when(playerRepository.findByPlayerName(player2.getPlayerName())).thenReturn(Optional.of(player2));
        Player player3 = new Player();
        player3.setId(4L);
        player3.setPlayerName("Jill");
        player3.setCardNumber(3);
        player3.setPot(0);
        when(playerRepository.findByPlayerName(player3.getPlayerName())).thenReturn(Optional.of(player3));
        PlayerCard playerCard4 = new PlayerCard();
        playerCard4.setCardValue(4);
        playerCard4.setFrontImagePath("card4.png");
        PlayerCard playerCard5 = new PlayerCard();
        playerCard5.setCardValue(8);
        playerCard5.setFrontImagePath("card5.png");
        PlayerCard playerCard6 = new PlayerCard();
        playerCard6.setCardValue(10);
        playerCard6.setFrontImagePath("card6.png");
        when(playerHandRepository.findAllByPlayerId(4L)).thenReturn(Optional.of(List.of(playerCard4, playerCard5, playerCard6)));
        Player player4 = new Player();
        player4.setId(5L);
        player4.setPlayerName("John");
        player4.setCardNumber(1);
        player4.setPot(6);
        when(playerRepository.findByPlayerName(player4.getPlayerName())).thenReturn(Optional.of(player4));
        Dealer dealer = new Dealer();
        dealer.setId(12L);
        dealer.setCardNumber(1);
        dealer.setBalance(90);
        when(dealerRepository.findById(12L)).thenReturn(Optional.of(dealer));

        GameMessage gameMessage = messageService.gameToMessage(game);

        assertEquals("Jill", gameMessage.getPlayer3());
        assertEquals(60, gameMessage.getPlayer2Balance());
        assertEquals(90, gameMessage.getDealerBalance());
        assertEquals(22, gameMessage.getRemainingCards());
        assertEquals("JILL busted and lost 2 $!", gameMessage.getContent());
        PublicHandDTO handDTO1 = new PublicHandDTO(List.of(
                new CardDTO(7, "card1.png"),
                new CardDTO(7, "card2.png"),
                new CardDTO(9, "card3.png")),
                23);
        assertEquals(handDTO1, gameMessage.getPlayer1PublicHand());
        PublicHandDTO handDTO2 = new PublicHandDTO(List.of(
                new CardDTO(4, "card4.png"),
                new CardDTO(8, "card5.png"),
                new CardDTO(10, "card6.png")),
                22);
        assertEquals(handDTO2, gameMessage.getPlayer3PublicHand());

    }

    private static Game getGame() {
        Game game = new Game();
        game.setGameId(1L);
        game.setPlayer1("Jane");
        game.setPlayer2("Jack");
        game.setPlayer3("Jill");
        game.setPlayer4("John");
        game.setPlayer1Balance(50);
        game.setPlayer2Balance(60);
        game.setPlayer3Balance(70);
        game.setPlayer4Balance(80);
        game.setDealerBalance(90);
        game.setTurnName("John");
        game.setRemainingCards(22);
        game.setState(GameState.IN_PROGRESS);
        game.setInformation("JILL busted and lost 2 $!");
        game.setLastCard(false);
        game.setPublicHand1Exists(true);
        game.setPublicHand2Exists(false);
        game.setPublicHand3Exists(true);
        game.setPublicHand4Exists(false);
        game.setDealerId(12L);
        return game;
    }
}
