package com.codecool.twentyone.service;

import com.codecool.twentyone.model.dto.GameMessage;
import com.codecool.twentyone.model.dto.websocketdto.CardDTO;
import com.codecool.twentyone.model.dto.websocketdto.DealerHandDTO;
import com.codecool.twentyone.model.dto.websocketdto.PlayerHandDTO;
import com.codecool.twentyone.model.entities.*;
import com.codecool.twentyone.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private DealerHandRepository dealerHandRepository;

    @Mock
    private DealerRepository dealerRepository;

    @Mock
    private ShuffleRepository shuffleRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerHandRepository playerHandRepository;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private GameService gameService;

    @Test
    void getFirstCard_shouldReturnPlayerHandDTO_when_gameIdAndPlayerNameIsValid() {
        Long gameId = 3L;
        String playerName = "john";
        Game currentGame = new Game();
        currentGame.setGameId(gameId);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(currentGame));
        Card firstCard = new Card();
        firstCard.setId(24);
        firstCard.setValue(11);
        firstCard.setFrontImagePath("24heart-ace.png");
        when(shuffleRepository.findCardByGameIdAndCardOrder(gameId, currentGame.getCardOrder())).thenReturn(Optional.of(firstCard));
        Player player = new Player();
        player.setPlayerName(playerName);
        when(playerRepository.findByPlayerName(playerName)).thenReturn(Optional.of(player));
        PlayerHandDTO expected = new PlayerHandDTO(PlayerState.WAITING_CARD, List.of(new CardDTO(11, "24heart-ace.png")), 11, "hand.firstUpdate");
        assertEquals(expected, gameService.getFirstCard(gameId, playerName));
        verify(playerHandRepository, times(1)).save(any(PlayerHand.class));
        verify(gameRepository, times(1)).save(currentGame);
        verify(playerRepository, times(1)).save(player);
    }

    @Test
    void giveDealerFirstCard_shouldSaveToDatabase_when_gameIdAndDealerIdIsValid() {
        Long gameId = 3L;
        Long dealerId = 2L;
        Game currentGame = new Game();
        currentGame.setGameId(gameId);
        currentGame.setDealerId(dealerId);
        currentGame.setCardOrder(3);
        currentGame.setPlayer1("john");
        currentGame.setPlayer2("jane");
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(currentGame));
        Card card = new Card();
        card.setId(24);
        card.setValue(11);
        card.setFrontImagePath("24heart-ace.png");
        when(shuffleRepository.findCardByGameIdAndCardOrder(gameId, currentGame.getCardOrder())).thenReturn(Optional.of(card));
        Dealer dealer = new Dealer();
        dealer.setId(dealerId);
        when(dealerRepository.findById(dealerId)).thenReturn(Optional.of(dealer));

        gameService.giveDealerFirstCard(gameId, dealerId);

        ArgumentCaptor<DealerHand> dealerHandArgumentCaptor = ArgumentCaptor.forClass(DealerHand.class);
        verify(dealerHandRepository, times(1)).save(dealerHandArgumentCaptor.capture());
        DealerHand dealerHand = dealerHandArgumentCaptor.getValue();
        assertEquals(11, dealerHand.getCardValue());
        assertEquals("24heart-ace.png", dealerHand.getFrontImagePath());

        ArgumentCaptor<Game> gameArgumentCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository, times(1)).save(gameArgumentCaptor.capture());
        Game game = gameArgumentCaptor.getValue();
        assertEquals(4, game.getCardOrder());
        assertEquals(2L, game.getDealerId());
        assertEquals("john", game.getPlayer1());
        assertEquals("jane", game.getPlayer2());

        ArgumentCaptor<Dealer> dealerArgumentCaptor = ArgumentCaptor.forClass(Dealer.class);
        verify(dealerRepository, times(1)).save(dealerArgumentCaptor.capture());
        Dealer capturedDealer = dealerArgumentCaptor.getValue();
        assertEquals(dealerId, capturedDealer.getId());
    }

    @Test
    void getDealerHand_shouldReturnDealerHand_when_gameIdIsValid() {
        Long gameId = 3L;
        Dealer dealer = new Dealer();
        dealer.setId(5L);
        dealer.setCardNumber(2);
        Game currentGame = new Game();
        currentGame.setGameId(gameId);
        currentGame.setDealerId(dealer.getId());
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(currentGame));
        List<DealerHand> dealerCards = new ArrayList<>();
        DealerHand card1 = new DealerHand();
        card1.setId(100L);
        card1.setCardValue(11);
        card1.setFrontImagePath("card1.png");
        card1.setDealer(dealer);
        dealerCards.add(card1);
        DealerHand card2 = new DealerHand();
        card2.setId(101L);
        card2.setCardValue(11);
        card2.setFrontImagePath("card2.png");
        card2.setDealer(dealer);
        dealerCards.add(card2);
        when(dealerHandRepository.findAllByDealerId(dealer.getId())).thenReturn(Optional.of(dealerCards));
        when(dealerHandRepository.getHandValue(dealer.getId())).thenReturn(22);
        DealerHandDTO expected = new DealerHandDTO(List.of(new CardDTO(11, "card1.png"), new CardDTO(11, "card2.png")), 22);
        DealerHandDTO actual = gameService.getDealerHand(gameId);
        assertEquals(expected, actual);
    }

    @Test
    void getDealerHand_shouldThrowException_whenGameIdIsInvalid() {
        Long gameId = 3L;
        Dealer dealer = new Dealer();
        dealer.setId(5L);
        dealer.setCardNumber(2);
        Game currentGame = new Game();
        currentGame.setGameId(gameId);
        currentGame.setDealerId(dealer.getId());
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> gameService.getDealerHand(gameId));
        assertEquals("Game not found", exception.getMessage());
    }

    @Test
    void getDealerHand_shouldThrowException_whenDealerIdIsInvalid() {
        Long gameId = 3L;
        Dealer dealer = new Dealer();
        dealer.setId(5L);
        dealer.setCardNumber(2);
        Game currentGame = new Game();
        currentGame.setGameId(gameId);
        currentGame.setDealerId(dealer.getId());
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(currentGame));
        when(dealerHandRepository.findAllByDealerId(dealer.getId())).thenReturn(Optional.empty());
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> gameService.getDealerHand(gameId));
        assertEquals("Cards not found", exception.getMessage());
    }

    @Test
    void pullCard_shouldReturnGameMessage_whenPlayerHasAnAceAndGetsAKing() {
        Player player = new Player();
        player.setId(5L);
        player.setPlayerName("John");
        player.setCardNumber(1);
        player.setPlayerState(PlayerState.WAITING_CARD);
        when(playerRepository.findByPlayerName(player.getPlayerName())).thenReturn(Optional.of(player));
        Game game = new Game();
        game.setGameId(3L);
        game.setDealerId(2L);
        game.setTurnName(player.getPlayerName());
        game.setRemainingCards(29);
        game.setCardOrder(4);
        game.setPlayer1(player.getPlayerName());
        game.setPlayer2("Jane");
        when(gameRepository.findById(game.getGameId())).thenReturn(Optional.of(game));
        Dealer dealer = new Dealer();
        dealer.setId(2L);
        dealer.setCardNumber(1);
        when(dealerRepository.findById(game.getDealerId())).thenReturn(Optional.of(dealer));
        Card nextCard = new Card();
        nextCard.setValue(4);
        when(shuffleRepository.findCardByGameIdAndCardOrder(game.getGameId(), game.getCardOrder())).thenReturn(Optional.of(nextCard));
        when(playerHandRepository.getHandValue(player.getId())).thenReturn(15);
        when(playerHandRepository.getHandSize(player.getId())).thenReturn(2);

        gameService.pullCard(game.getGameId(), player.getPlayerName());

        assertEquals(PlayerState.COULD_STOP, player.getPlayerState());
        assertEquals(28, game.getRemainingCards());
        assertEquals(5, game.getCardOrder());

        verify(playerHandRepository, times(1)).save(any(PlayerHand.class));
        verify(gameRepository, times(1)).save(any(Game.class));
        verify(dealerRepository, times(1)).save(any(Dealer.class));
        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    void pullCard_shouldReturnGameMessage_when_PlayerHasAnAceAndGetsAnotherAce() {
        Player player = new Player();
        player.setId(5L);
        player.setPlayerName("John");
        player.setCardNumber(1);
        player.setPlayerState(PlayerState.WAITING_CARD);
        when(playerRepository.findByPlayerName(player.getPlayerName())).thenReturn(Optional.of(player));
        Game game = new Game();
        game.setGameId(3L);
        game.setDealerId(2L);
        game.setTurnName(player.getPlayerName());
        game.setRemainingCards(29);
        game.setCardOrder(4);
        game.setPlayer1(player.getPlayerName());
        game.setPlayer2("Jane");
        when(gameRepository.findById(game.getGameId())).thenReturn(Optional.of(game));
        Dealer dealer = new Dealer();
        dealer.setId(2L);
        dealer.setCardNumber(1);
        when(dealerRepository.findById(game.getDealerId())).thenReturn(Optional.of(dealer));
        Card nextCard = new Card();
        nextCard.setValue(11);
        when(shuffleRepository.findCardByGameIdAndCardOrder(game.getGameId(), game.getCardOrder())).thenReturn(Optional.of(nextCard));
        when(playerHandRepository.getHandValue(player.getId())).thenReturn(22);
        when(playerHandRepository.getHandSize(player.getId())).thenReturn(2);
        when(playerRepository.getPlayerStateByPlayerName(game.getPlayer2())).thenReturn(PlayerState.WAITING_CARD);

        gameService.pullCard(game.getGameId(), player.getPlayerName());

        assertEquals(PlayerState.FIRE, player.getPlayerState());
        assertEquals(28, game.getRemainingCards());
        assertEquals(5, game.getCardOrder());
        assertEquals("Jane", game.getTurnName());

        verify(playerHandRepository, times(1)).save(any(PlayerHand.class));
        verify(gameRepository, times(1)).save(any(Game.class));
        verify(dealerRepository, times(1)).save(any(Dealer.class));
        verify(playerRepository, times(1)).save(any(Player.class));
        verify(playerRepository, times(1)).getPlayerStateByPlayerName(game.getPlayer2());
    }

    @Test
    void pullCard_shouldReturnGameMessage_when_PlayerHasAnAceAndGetsANine() {
        Player player = new Player();
        player.setId(5L);
        player.setPlayerName("John");
        player.setCardNumber(1);
        player.setPlayerState(PlayerState.WAITING_CARD);
        when(playerRepository.findByPlayerName(player.getPlayerName())).thenReturn(Optional.of(player));
        Game game = new Game();
        game.setGameId(3L);
        game.setDealerId(2L);
        game.setTurnName(player.getPlayerName());
        game.setRemainingCards(30);
        game.setCardOrder(3);
        game.setPlayer1(player.getPlayerName());
        when(gameRepository.findById(game.getGameId())).thenReturn(Optional.of(game));
        Dealer dealer = new Dealer();
        dealer.setId(2L);
        dealer.setCardNumber(1);
        when(dealerRepository.findById(game.getDealerId())).thenReturn(Optional.of(dealer));
        Card nextCard = new Card();
        nextCard.setValue(9);
        when(shuffleRepository.findCardByGameIdAndCardOrder(game.getGameId(), game.getCardOrder())).thenReturn(Optional.of(nextCard));
        when(playerHandRepository.getHandValue(player.getId())).thenReturn(20);
        when(playerHandRepository.getHandSize(player.getId())).thenReturn(2);

        gameService.pullCard(game.getGameId(), player.getPlayerName());

        assertEquals(PlayerState.ENOUGH, player.getPlayerState());
        assertEquals(29, game.getRemainingCards());
        assertEquals(4, game.getCardOrder());
        assertEquals("Dealer", game.getTurnName());

        verify(playerHandRepository, times(1)).save(any(PlayerHand.class));
        verify(gameRepository, times(1)).save(any(Game.class));
        verify(dealerRepository, times(1)).save(any(Dealer.class));
        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    void pullCard_shouldReturnGameMessage_when_playerHandValueIs15AndGetsAnAce() {
        Player player = new Player();
        player.setId(5L);
        player.setPlayerName("John");
        player.setCardNumber(2);
        player.setPlayerState(PlayerState.COULD_STOP);
        when(playerRepository.findByPlayerName(player.getPlayerName())).thenReturn(Optional.of(player));
        Game game = new Game();
        game.setGameId(3L);
        game.setDealerId(2L);
        game.setTurnName(player.getPlayerName());
        game.setRemainingCards(29);
        game.setCardOrder(4);
        game.setPlayer1(player.getPlayerName());
        game.setPlayer2("Jane");
        when(gameRepository.findById(game.getGameId())).thenReturn(Optional.of(game));
        Dealer dealer = new Dealer();
        dealer.setId(2L);
        dealer.setCardNumber(1);
        when(dealerRepository.findById(game.getDealerId())).thenReturn(Optional.of(dealer));
        Card nextCard = new Card();
        nextCard.setValue(11);
        when(shuffleRepository.findCardByGameIdAndCardOrder(game.getGameId(), game.getCardOrder())).thenReturn(Optional.of(nextCard));
        when(playerHandRepository.getHandValue(player.getId())).thenReturn(26);
        when(playerHandRepository.getHandSize(player.getId())).thenReturn(3);
        when(playerRepository.getPlayerStateByPlayerName(game.getPlayer2())).thenReturn(PlayerState.WAITING_CARD);

        gameService.pullCard(game.getGameId(), player.getPlayerName());

        assertEquals(PlayerState.MUCH, player.getPlayerState());
        assertEquals(28, game.getRemainingCards());
        assertEquals(5, game.getCardOrder());
        assertEquals("Jane", game.getTurnName());

        verify(playerHandRepository, times(1)).save(any(PlayerHand.class));
        verify(gameRepository, times(1)).save(any(Game.class));
        verify(dealerRepository, times(1)).save(any(Dealer.class));
        verify(playerRepository, times(1)).save(any(Player.class));
        verify(playerRepository, times(1)).getPlayerStateByPlayerName(game.getPlayer2());
    }

}
