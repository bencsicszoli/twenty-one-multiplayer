package com.codecool.twentyone.service;

import com.codecool.twentyone.model.dto.GameMessage;
import com.codecool.twentyone.model.dto.websocketdto.CardDTO;
import com.codecool.twentyone.model.dto.websocketdto.DealerHandDTO;
import com.codecool.twentyone.model.dto.websocketdto.PlayerHandDTO;
import com.codecool.twentyone.model.dto.websocketdto.PlayerStateDTO;
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
    void raiseBet_shouldModifyGame_when_gameIdAndBetIsValid() {
        Long gameId = 3L;
        String turnName = "john";
        int bet = 3;
        Game game = new Game();
        game.setGameId(gameId);
        game.setTurnName(turnName);
        game.setPlayer2(turnName);
        game.setPlayer2Balance(60);
        game.setDealerId(6L);
        game.setDealerBalance(80);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        Dealer dealer = new Dealer();
        dealer.setId(6L);
        dealer.setBalance(game.getDealerBalance());
        when(dealerRepository.findById(game.getDealerId())).thenReturn(Optional.of(dealer));
        Player player = new Player();
        player.setPlayerName(turnName);
        player.setBalance(game.getPlayer2Balance());
        player.setPot(0);
        player.setPlayerState(PlayerState.WAITING_CARD);
        when(playerRepository.findByPlayerName(turnName)).thenReturn(Optional.of(player));

        gameService.raiseBet(gameId, turnName, bet);

        ArgumentCaptor<Game> gameArgumentCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository, times(1)).save(gameArgumentCaptor.capture());
        Game capturedGame = gameArgumentCaptor.getValue();
        assertEquals(57, capturedGame.getPlayer2Balance());
        assertEquals("JOHN placed a 3 $ bet!", capturedGame.getInformation());
        assertEquals(77, capturedGame.getDealerBalance());

        ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository, times(1)).save(playerCaptor.capture());
        Player capturedPlayer = playerCaptor.getValue();
        assertEquals(57, capturedPlayer.getBalance());
        assertEquals(6, capturedPlayer.getPot());

        ArgumentCaptor<Dealer> dealerCaptor = ArgumentCaptor.forClass(Dealer.class);
        verify(dealerRepository, times(1)).save(dealerCaptor.capture());
        Dealer capturedDealer = dealerCaptor.getValue();
        assertEquals(77, capturedDealer.getBalance());
    }

    @Test
    void raiseBet_shouldThrowException_when_betIsNegative() {
        Long gameId = 3L;
        String turnName = "john";
        int bet = -2;

        RuntimeException exception = assertThrows(RuntimeException.class, () -> gameService.raiseBet(gameId, turnName, bet));
        assertEquals("Bet cannot be negative", exception.getMessage());
    }

    @Test
    void raiseBet_shouldThrowException_when_betIsMoreThanPlayerBalance() {
        Long gameId = 3L;
        String turnName = "john";
        int bet = 30;
        Game game = new Game();
        game.setGameId(gameId);
        game.setTurnName(turnName);
        game.setPlayer1(turnName);
        game.setPlayer1Balance(20);
        game.setDealerId(6L);
        game.setDealerBalance(80);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        Dealer dealer = new Dealer();
        dealer.setId(6L);
        dealer.setBalance(game.getDealerBalance());
        when(dealerRepository.findById(game.getDealerId())).thenReturn(Optional.of(dealer));
        Player player = new Player();
        player.setPlayerName(turnName);
        player.setBalance(game.getPlayer1Balance());
        player.setPot(0);
        player.setPlayerState(PlayerState.WAITING_CARD);
        when(playerRepository.findByPlayerName(turnName)).thenReturn(Optional.of(player));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> gameService.raiseBet(gameId, turnName, bet));
        assertEquals("Player's balance is less than bet", exception.getMessage());
    }

    @Test
    void raiseBet_shouldThrowException_when_betIsMoreThanDealerBalance() {
        Long gameId = 3L;
        String turnName = "john";
        int bet = 30;
        Game game = new Game();
        game.setGameId(gameId);
        game.setTurnName(turnName);
        game.setPlayer3(turnName);
        game.setPlayer3Balance(80);
        game.setDealerId(6L);
        game.setDealerBalance(25);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        Dealer dealer = new Dealer();
        dealer.setId(6L);
        dealer.setBalance(game.getDealerBalance());
        when(dealerRepository.findById(game.getDealerId())).thenReturn(Optional.of(dealer));
        Player player = new Player();
        player.setPlayerName(turnName);
        player.setBalance(game.getPlayer3Balance());
        player.setPot(0);
        player.setPlayerState(PlayerState.WAITING_CARD);
        when(playerRepository.findByPlayerName(turnName)).thenReturn(Optional.of(player));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> gameService.raiseBet(gameId, turnName, bet));
        assertEquals("Dealer balance is less than bet", exception.getMessage());
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

    @Test
    void getPlayerState_shouldReturnPlayerStateDTO_when_PlayerNameIsValid() {
        String playerName = "John";
        PlayerState playerState = PlayerState.OHNE_ACE;
        when(playerRepository.getPlayerStateByPlayerName(playerName)).thenReturn(playerState);
        PlayerStateDTO expected = new PlayerStateDTO(playerState.toString(), "playerState.update");
        assertEquals(expected, gameService.getPlayerState(playerName));
    }

    @Test
    void passTurnWhenStand_shouldModifyGame_when_handValueIs16AndPlayerStateIsCOULD_STOP() {
        Long gameId = 3L;
        String playerName = "John";
        Player player = new Player();
        player.setPlayerName(playerName);
        player.setPlayerState(PlayerState.COULD_STOP);
        when(playerRepository.findByPlayerName(playerName)).thenReturn(Optional.of(player));
        when(playerHandRepository.getHandValue(player.getId())).thenReturn(16);
        Game game = new Game();
        game.setGameId(gameId);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        gameService.passTurnWhenStand(gameId, playerName);

        assertEquals("JOHN announced 'stand'", game.getInformation());
        ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository, times(1)).save(captor.capture());
        assertEquals(PlayerState.ENOUGH, captor.getValue().getPlayerState());
    }

    @Test
    void passTurnWhenStand_shouldThrowRuntimeException_when_handValueIs13() {
        Long gameId = 3L;
        String playerName = "John";
        Player player = new Player();
        player.setPlayerName(playerName);
        player.setPlayerState(PlayerState.WAITING_CARD);
        when(playerRepository.findByPlayerName(playerName)).thenReturn(Optional.of(player));
        when(playerHandRepository.getHandValue(player.getId())).thenReturn(13);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> gameService.passTurnWhenStand(gameId, playerName));
        assertEquals("You cannot stop under 15", exception.getMessage());
    }

    @Test
    void passTurnWhenStand_shouldThrowRuntimeException_when_handValueIs16AndPlayerStateIsWAITING_CARD() {
        Long gameId = 3L;
        String playerName = "John";
        Player player = new Player();
        player.setPlayerName(playerName);
        player.setPlayerState(PlayerState.WAITING_CARD);
        when(playerRepository.findByPlayerName(playerName)).thenReturn(Optional.of(player));
        when(playerHandRepository.getHandValue(player.getId())).thenReturn(16);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> gameService.passTurnWhenStand(gameId, playerName));
        assertEquals("You must take another card after placing a bet", exception.getMessage());
    }

    @Test
    void getHand_shouldReturnPlayerHandDTO_when_PlayerStateIsOhneAce() {
        Player player = new Player();
        player.setId(5L);
        player.setPlayerName("John");
        player.setPlayerState(PlayerState.OHNE_ACE);
        when(playerRepository.findByPlayerName(player.getPlayerName())).thenReturn(Optional.of(player));
        PlayerHand card1 = new PlayerHand();
        card1.setCardValue(9);
        card1.setFrontImagePath("card1.png");
        PlayerHand card2 = new PlayerHand();
        card2.setCardValue(2);
        card2.setFrontImagePath("card2.png");
        when(playerHandRepository.findAllByPlayerId(player.getId())).thenReturn(Optional.of(List.of(card1, card2)));
        PlayerHandDTO expected = new PlayerHandDTO(player.getPlayerState(), List.of(new CardDTO(card1.getCardValue(), card1.getFrontImagePath()), new CardDTO(card2.getCardValue(), card2.getFrontImagePath())), 11, "hand.withOhneAce");
        assertEquals(expected, gameService.getHand(player.getPlayerName()));
    }

    @Test
    void getHand_shouldReturnPlayerHandDTO_when_PlayerStateIsNotOhneAce() {
        Player player = new Player();
        player.setId(5L);
        player.setPlayerName("John");
        player.setPlayerState(PlayerState.COULD_STOP);
        when(playerRepository.findByPlayerName(player.getPlayerName())).thenReturn(Optional.of(player));
        PlayerHand card1 = new PlayerHand();
        card1.setCardValue(9);
        card1.setFrontImagePath("card1.png");
        PlayerHand card2 = new PlayerHand();
        card2.setCardValue(4);
        card2.setFrontImagePath("card2.png");
        when(playerHandRepository.findAllByPlayerId(player.getId())).thenReturn(Optional.of(List.of(card1, card2)));
        PlayerHandDTO expected = new PlayerHandDTO(
                player.getPlayerState(),
                List.of(
                        new CardDTO(card1.getCardValue(), card1.getFrontImagePath()),
                        new CardDTO(card2.getCardValue(), card2.getFrontImagePath())),
                13,
                "hand.update");
        assertEquals(expected, gameService.getHand(player.getPlayerName()));
    }

    @Test
    void setPlayerStateToOhneAce_shouldReturnPlayerStateDTO_when_PlayerStateIsNotOhneAce() {
        String playerName = "John";
        PlayerStateDTO expected = new PlayerStateDTO(PlayerState.OHNE_ACE.toString(), "playerState.update");
        assertEquals(expected, gameService.setPlayerStateToOhneAce(playerName));
        verify(playerRepository, times(1)).setPlayerStateByPlayerName(playerName);
    }

    @Test
    void setContent_shouldSetNewContent_when_gameIdIsValid() {
        Long gameId = 2L;
        String content = "John discarded an ace after announcing Ohne Ace";
        Game game = new Game();
        game.setGameId(2L);
        game.setInformation("John announced Ohne Ace");
        when(gameRepository.findById(game.getGameId())).thenReturn(Optional.of(game));

        gameService.setContent(gameId, content);

        ArgumentCaptor<Game> gameArgumentCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository, times(1)).save(gameArgumentCaptor.capture());
        assertEquals("John discarded an ace after announcing Ohne Ace", gameArgumentCaptor.getValue().getInformation());
    }

    @Test
    void throwAce_shouldReturnPlayerHandDTO_when_playerNameIsValid() {
        String playerName = "John";
        Player player = new Player();
        player.setId(5L);
        player.setPlayerName(playerName);
        player.setCardNumber(3);
        when(playerRepository.findByPlayerName(playerName)).thenReturn(Optional.of(player));
        PlayerHand card1 = new PlayerHand();
        card1.setCardValue(9);
        card1.setFrontImagePath("card1.png");
        PlayerHand card2 = new PlayerHand();
        card2.setCardValue(2);
        card2.setFrontImagePath("card2.png");
        when(playerHandRepository.findAllByPlayerId(player.getId())).thenReturn(Optional.of(List.of(card1, card2)));
        PlayerHandDTO expected = new PlayerHandDTO(
                PlayerState.WAITING_CARD,
                List.of(
                        new CardDTO(card1.getCardValue(), card1.getFrontImagePath()),
                        new CardDTO(card2.getCardValue(), card2.getFrontImagePath())), 11, "game.throwAce");
        assertEquals(expected, gameService.throwAce(playerName));

        ArgumentCaptor<Player> playerArgumentCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository, times(1)).save(playerArgumentCaptor.capture());
        verify(playerHandRepository, times(1)).deleteAceFromHand(player.getId());
        assertEquals(2, playerArgumentCaptor.getValue().getCardNumber());
    }

    @Test
    void throwCards_shouldModifyGame_when_playerNameAndGameIdAreValid() {
        Long gameId = 2L;
        String playerName = "John";
        Player player = new Player();
        player.setPlayerName(playerName);
        player.setPlayerState(PlayerState.COULD_STOP);
        player.setCardNumber(5);
        when(playerRepository.findByPlayerName(playerName)).thenReturn(Optional.of(player));
        Game game = new Game();
        game.setGameId(gameId);
        game.setInformation("any content");
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        gameService.throwCards(playerName, gameId);

        ArgumentCaptor<Game> gameArgumentCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository, times(1)).save(gameArgumentCaptor.capture());
        assertEquals("JOHN discarded 5 cards!", gameArgumentCaptor.getValue().getInformation());
        ArgumentCaptor<Player> playerArgumentCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository, times(1)).save(playerArgumentCaptor.capture());
        assertEquals(PlayerState.WAITING_CARD, playerArgumentCaptor.getValue().getPlayerState());
        assertEquals(0, playerArgumentCaptor.getValue().getCardNumber());
        verify(playerHandRepository, times(1)).deleteAllByPlayerId(player.getId());



    }

}
