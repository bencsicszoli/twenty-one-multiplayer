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
import static org.junit.jupiter.api.Assertions.*;
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
    void handleDealerTurn_shouldReturnOneMessage_when_activePlayersAreZero() {
        Game game = new Game();
        game.setPlayer1("John");
        game.setPlayer3("Jane");
        game.setGameId(5L);
        game.setDealerId(8L);
        game.setState(GameState.IN_PROGRESS);
        game.setCardOrder(6);
        Player player1 = new Player();
        player1.setPlayerName(game.getPlayer1());
        player1.setPlayerState(PlayerState.MUCH);
        when(playerRepository.findByPlayerName(player1.getPlayerName())).thenReturn(Optional.of(player1));
        Player player2 = new Player();
        player2.setPlayerName(game.getPlayer3());
        player2.setPlayerState(PlayerState.FIRE);
        when(playerRepository.findByPlayerName(player2.getPlayerName())).thenReturn(Optional.of(player2));
        GameMessage gameMessage = new GameMessage();
        when(messageService.gameToMessage(game)).thenReturn(gameMessage);
        when(gameRepository.findById(game.getGameId())).thenReturn(Optional.of(game));
        DealerHand dealerHand = new DealerHand();
        dealerHand.setCardValue(3);
        dealerHand.setFrontImagePath("card.png");
        List<DealerHand> cards = List.of(dealerHand);
        when(dealerHandRepository.findAllByDealerId(game.getDealerId())).thenReturn(Optional.of(cards));
        int dealerHandValue = 3;
        when(dealerHandRepository.getHandValue(game.getDealerId())).thenReturn(dealerHandValue);

        List<GameMessage> messages = gameService.handleDealerTurn(game);

        assertEquals(1, messages.size());
        assertEquals(GameState.NEW, game.getState());
        assertEquals(1, game.getCardOrder());
        CardDTO dto = new CardDTO(3, "card.png");
        DealerHandDTO expected = new DealerHandDTO(List.of(dto), 3);
        assertEquals(expected, messages.getFirst().getDealerPublicHand());
    }

    @Test
    void handleDealerTurn_shouldReturnMoreMessages_when_twoActivePlayers() {
        Game game = new Game();
        game.setPlayer2("John");
        game.setPlayer4("Jane");
        game.setDealerId(8L);
        game.setGameId(5L);
        game.setCardOrder(6);
        game.setDealerBalance(80);
        game.setState(GameState.IN_PROGRESS);
        game.setCardOrder(6);
        game.setPlayer2Balance(50);
        game.setPlayer4Balance(60);
        game.setRemainingCards(27);
        when(gameRepository.findById(game.getGameId())).thenReturn(Optional.of(game));

        Player player1 = new Player();
        player1.setPlayerName(game.getPlayer2());
        player1.setPlayerState(PlayerState.ENOUGH);
        player1.setId(3L);
        player1.setPot(4);
        player1.setGames(5);
        player1.setWins(3);
        player1.setLosses(2);
        player1.setBalance(game.getPlayer2Balance());
        when(playerRepository.findByPlayerName(player1.getPlayerName())).thenReturn(Optional.of(player1));

        Player player2 = new Player();
        player2.setPlayerName(game.getPlayer4());
        player2.setPlayerState(PlayerState.ENOUGH);
        player2.setId(4L);
        player2.setPot(6);
        player2.setGames(7);
        player2.setWins(2);
        player2.setLosses(5);
        player2.setBalance(game.getPlayer4Balance());
        when(playerRepository.findByPlayerName(player2.getPlayerName())).thenReturn(Optional.of(player2));

        Dealer dealer = new Dealer();
        dealer.setId(game.getDealerId());
        dealer.setBalance(game.getDealerBalance());
        when(dealerRepository.findById(game.getDealerId())).thenReturn(Optional.of(dealer));

        DealerHand dealerCard1 = new DealerHand();
        dealerCard1.setCardValue(11);
        dealerCard1.setFrontImagePath("card1.png");
        dealerCard1.setDealer(dealer);

        GameMessage gameMessage = new GameMessage();
        when(messageService.gameToMessage(game)).thenReturn(gameMessage);

        Card newCard = new Card();
        newCard.setValue(7);
        newCard.setFrontImagePath("card2.png");
        when(shuffleRepository.findCardByGameIdAndCardOrder(game.getGameId(), game.getCardOrder())).thenReturn(Optional.of(newCard));

        DealerHand dealerCard2 = new DealerHand();
        dealerCard2.setDealer(dealer);
        dealerCard2.setFrontImagePath(newCard.getFrontImagePath());
        dealerCard2.setCardValue(newCard.getValue());

        when(playerHandRepository.getHandValue(player1.getId())).thenReturn(17);
        when(playerHandRepository.getHandValue(player2.getId())).thenReturn(19);

        when(dealerHandRepository.findAllByDealerId(dealer.getId()))
                .thenReturn(Optional.of(List.of(dealerCard1)))
                .thenReturn(Optional.of(List.of(dealerCard1, dealerCard2)))
                .thenReturn(Optional.of(List.of(dealerCard1, dealerCard2)));
        when(dealerHandRepository.getHandValue(dealer.getId()))
                .thenReturn(dealerCard1.getCardValue())
                .thenReturn(dealerCard1.getCardValue() + dealerCard2.getCardValue())
                .thenReturn(dealerCard1.getCardValue() + dealerCard2.getCardValue());

        List<GameMessage> messages = gameService.handleDealerTurn(game);

        assertEquals(3, messages.size());
        assertEquals(GameState.NEW, game.getState());
        assertEquals(1, game.getCardOrder());
        assertEquals(50, game.getPlayer2Balance());
        assertEquals(66, game.getPlayer4Balance());
        assertEquals(84, game.getDealerBalance());
        assertEquals(26, game.getRemainingCards());
        CardDTO dto1 = new CardDTO(11, "card1.png");
        CardDTO dto2 = new CardDTO(7, "card2.png");
        DealerHandDTO expected = new DealerHandDTO(List.of(dto1, dto2), 18);
        assertEquals(expected, messages.getLast().getDealerPublicHand());
        assertEquals("DEALER won 2 $ from JOHN!JANE won 3 $!", game.getInformation());
        assertEquals(6, player1.getGames());
        assertEquals(8, player2.getGames());
        assertEquals(3, player1.getLosses());
        assertEquals(3, player2.getWins());
        assertEquals(0, player1.getPot());
        assertEquals(0, player2.getPot());
    }

    @Test
    void handleDealerTurn_shouldDiscardCards_when_hasFiveCards_AndTwoActivePlayers() {

        //handleDealerTurn
        Game game = new Game();
        game.setPlayer2("John");
        game.setPlayer4("Jane");
        game.setDealerId(8L);
        game.setGameId(5L);
        game.setCardOrder(6);
        game.setDealerBalance(80);
        game.setState(GameState.IN_PROGRESS);
        game.setCardOrder(6);
        game.setPlayer2Balance(50);
        game.setPlayer4Balance(60);
        game.setRemainingCards(27);
        when(gameRepository.findById(game.getGameId())).thenReturn(Optional.of(game));

        //getPlayerWithActiveHands
        //addPlayerWithActiveHand
        Player player1 = new Player();
        player1.setPlayerName(game.getPlayer2());
        player1.setPlayerState(PlayerState.ENOUGH);
        player1.setId(3L);
        player1.setPot(4);
        player1.setGames(5);
        player1.setWins(3);
        player1.setLosses(2);
        player1.setBalance(game.getPlayer2Balance());
        when(playerRepository.findByPlayerName(player1.getPlayerName())).thenReturn(Optional.of(player1));

        Player player2 = new Player();
        player2.setPlayerName(game.getPlayer4());
        player2.setPlayerState(PlayerState.ENOUGH);
        player2.setId(4L);
        player2.setPot(6);
        player2.setGames(7);
        player2.setWins(2);
        player2.setLosses(5);
        player2.setBalance(game.getPlayer4Balance());
        when(playerRepository.findByPlayerName(player2.getPlayerName())).thenReturn(Optional.of(player2));

        //processWithActivePlayerHands
        Dealer dealer = new Dealer();
        dealer.setId(game.getDealerId());
        dealer.setBalance(game.getDealerBalance());
        when(dealerRepository.findById(game.getDealerId())).thenReturn(Optional.of(dealer));

        //getDealerHand először, az első lapot adja vissza
        DealerHand dealerCard1 = new DealerHand();
        dealerCard1.setCardValue(2);
        dealerCard1.setFrontImagePath("card1.png");
        dealerCard1.setDealer(dealer);


        //addMessageWithFirsCard
        GameMessage gameMessage = new GameMessage();
        when(messageService.gameToMessage(game)).thenReturn(gameMessage);

        //getMinHandValue
        //dealerPullsCards
        //automaticCardPulling
        Card card2 = new Card(); //ez a második lap
        card2.setValue(3);
        card2.setFrontImagePath("card2.png");
        //addNormalDealerMessage
        //getDealerHand másodszor, a második kártyát adja vissza
        DealerHand dealerCard2 = new DealerHand();
        dealerCard2.setCardValue(3);
        dealerCard2.setFrontImagePath("card2.png");
        dealerCard2.setDealer(dealer);

        //automaticCardPulling
        Card card3 = new Card(); //ez a harmadik lap
        card3.setValue(4);
        card3.setFrontImagePath("card3.png");

        //addNormalDealerMessage
        //getDealerHand harmadszor, a harmadik kártyát adja vissza
        DealerHand dealerCard3 = new DealerHand();
        dealerCard3.setCardValue(4);
        dealerCard3.setFrontImagePath("card3.png");
        dealerCard3.setDealer(dealer);

        //automaticCardPulling
        Card card4 = new Card(); //ez a negyedik lap
        card4.setValue(3);
        card4.setFrontImagePath("card4.png");

        //addNormalDealerMessage
        //getDealerHand negyedszer, a negyedik kártyát adja vissza
        DealerHand dealerCard4 = new DealerHand();
        dealerCard4.setCardValue(3);
        dealerCard4.setFrontImagePath("card4.png");
        dealerCard4.setDealer(dealer);

        //automaticCardPulling
        Card card5 = new Card(); //ez az ötödik lap
        card5.setValue(4);
        card5.setFrontImagePath("card5.png");

        //dealerDiscardsFiveCards
        //getDealerHand ötödször, az ötödik kártyát adja vissza
        DealerHand dealerCard5 = new DealerHand();
        dealerCard5.setCardValue(4);
        dealerCard5.setFrontImagePath("card5.png");
        dealerCard5.setDealer(dealer);

        //automaticCardPulling
        Card card6 = new Card(); //ez a hatodik (első) lap
        card6.setValue(11);
        card6.setFrontImagePath("card6.png");

        //addNormalDealerMessage
        //getDealerHand hatodszor, a hatodik kártyát adja vissza
        DealerHand dealerCard6 = new DealerHand();
        dealerCard6.setCardValue(11);
        dealerCard6.setFrontImagePath("card6.png");
        dealerCard6.setDealer(dealer);

        //automaticCardPulling
        Card card7 = new Card(); //ez a hetedik (második) lap
        card7.setValue(7);
        card7.setFrontImagePath("card7.png");

        //addNormalDealerMessage
        //getDealerHand hetedszer, a hetedik kártyát adja vissza
        DealerHand dealerCard7 = new DealerHand();
        dealerCard7.setCardValue(7);
        dealerCard7.setFrontImagePath("card7.png");
        dealerCard7.setDealer(dealer);
        when(dealerHandRepository.findAllByDealerId(dealer.getId()))
                .thenReturn(Optional.of(List.of(dealerCard1)))
                .thenReturn(Optional.of(List.of(dealerCard1, dealerCard2)))
                .thenReturn(Optional.of(List.of(dealerCard1, dealerCard2, dealerCard3)))
                .thenReturn(Optional.of(List.of(dealerCard1, dealerCard2, dealerCard3, dealerCard4)))
                .thenReturn(Optional.of(List.of(dealerCard1, dealerCard2, dealerCard3, dealerCard4, dealerCard5)))
                .thenReturn(Optional.of(List.of(dealerCard6)))
                .thenReturn(Optional.of(List.of(dealerCard6, dealerCard7)))
                .thenReturn(Optional.of(List.of(dealerCard6, dealerCard7)));
        when(dealerHandRepository.getHandValue(dealer.getId()))
                .thenReturn(2)
                .thenReturn(5)
                .thenReturn(9)
                .thenReturn(12)
                .thenReturn(16)
                .thenReturn(11)
                .thenReturn(18)
                .thenReturn(18);
        when(shuffleRepository.findCardByGameIdAndCardOrder(
                eq(game.getGameId()),
                anyInt()))
                .thenReturn(Optional.of(card2))
                .thenReturn(Optional.of(card3))
                .thenReturn(Optional.of(card4))
                .thenReturn(Optional.of(card5))
                .thenReturn(Optional.of(card6))
                .thenReturn(Optional.of(card7));

        when(playerHandRepository.getHandValue(player1.getId())).thenReturn(19);
        when(playerHandRepository.getHandValue(player2.getId())).thenReturn(17);

        List<GameMessage> messages = gameService.handleDealerTurn(game);

        assertEquals(9, messages.size());
        assertEquals(GameState.NEW, game.getState());
        assertEquals(1, game.getCardOrder());
        assertEquals(54, game.getPlayer2Balance());
        assertEquals(60, game.getPlayer4Balance());
        assertEquals(86, game.getDealerBalance());
        assertEquals(21, game.getRemainingCards());
        CardDTO dto1 = new CardDTO(11, "card6.png");
        CardDTO dto2 = new CardDTO(7, "card7.png");
        DealerHandDTO expected = new DealerHandDTO(List.of(dto1, dto2), 18);
        assertEquals(expected, messages.getLast().getDealerPublicHand());
        assertEquals("JOHN won 2 $!DEALER won 3 $ from JANE!", game.getInformation());
        assertEquals(6, player1.getGames());
        assertEquals(8, player2.getGames());
        assertEquals(4, player1.getWins());
        assertEquals(6, player2.getLosses());
        assertEquals(0, player1.getPot());
        assertEquals(0, player2.getPot());
    }

    @Test
    void handleDealerTurn_shouldAnnounceOhneAce_when_twoActivePlayers() {

        //handleDealerTurn
        Game game = new Game();
        game.setPlayer1("John");
        game.setPlayer3("Jane");
        game.setDealerId(8L);
        game.setGameId(5L);
        game.setCardOrder(6);
        game.setDealerBalance(80);
        game.setState(GameState.IN_PROGRESS);
        game.setCardOrder(6);
        game.setPlayer1Balance(50);
        game.setPlayer3Balance(60);
        game.setRemainingCards(27);
        when(gameRepository.findById(game.getGameId())).thenReturn(Optional.of(game));

        //getPlayerWithActiveHands
        //addPlayerWithActiveHand
        Player player1 = new Player();
        player1.setPlayerName(game.getPlayer1());
        player1.setPlayerState(PlayerState.ENOUGH);
        player1.setId(3L);
        player1.setPot(4);
        player1.setGames(5);
        player1.setWins(3);
        player1.setLosses(2);
        player1.setBalance(game.getPlayer1Balance());
        when(playerRepository.findByPlayerName(player1.getPlayerName())).thenReturn(Optional.of(player1));

        Player player2 = new Player();
        player2.setPlayerName(game.getPlayer3());
        player2.setPlayerState(PlayerState.ENOUGH);
        player2.setId(4L);
        player2.setPot(6);
        player2.setGames(7);
        player2.setWins(2);
        player2.setLosses(5);
        player2.setBalance(game.getPlayer3Balance());
        when(playerRepository.findByPlayerName(player2.getPlayerName())).thenReturn(Optional.of(player2));

        //processWithActivePlayerHands
        Dealer dealer = new Dealer();
        dealer.setId(game.getDealerId());
        dealer.setBalance(game.getDealerBalance());
        when(dealerRepository.findById(game.getDealerId())).thenReturn(Optional.of(dealer));

        //getDealerHand először, az első lapot adja vissza
        DealerHand dealerCard1 = new DealerHand();
        dealerCard1.setCardValue(2);
        dealerCard1.setFrontImagePath("card1.png");
        dealerCard1.setDealer(dealer);


        //addMessageWithFirsCard
        GameMessage gameMessage = new GameMessage();
        when(messageService.gameToMessage(game)).thenReturn(gameMessage);

        //getMinHandValue
        //dealerPullsCards
        //automaticCardPulling
        Card card2 = new Card(); //ez a második lap
        card2.setValue(9);
        card2.setFrontImagePath("card2.png");
        //addNormalDealerMessage
        //getDealerHand másodszor, a második kártyát adja vissza
        DealerHand dealerCard2 = new DealerHand();
        dealerCard2.setCardValue(9);
        dealerCard2.setFrontImagePath("card2.png");
        dealerCard2.setDealer(dealer);

        //automaticCardPulling
        Card card3 = new Card(); //ez a harmadik lap
        card3.setValue(7);
        card3.setFrontImagePath("card3.png");

        //addNormalDealerMessage
        //getDealerHand harmadszor, a harmadik kártyát adja vissza
        DealerHand dealerCard3 = new DealerHand();
        dealerCard3.setCardValue(7);
        dealerCard3.setFrontImagePath("card3.png");
        dealerCard3.setDealer(dealer);

        when(dealerHandRepository.findAllByDealerId(dealer.getId()))
                .thenReturn(Optional.of(List.of(dealerCard1)))
                .thenReturn(Optional.of(List.of(dealerCard1, dealerCard2)))
                .thenReturn(Optional.of(List.of(dealerCard1, dealerCard2, dealerCard3)));
        when(dealerHandRepository.getHandValue(dealer.getId()))
                .thenReturn(2)
                .thenReturn(11)
                .thenReturn(18);
        when(shuffleRepository.findCardByGameIdAndCardOrder(
                eq(game.getGameId()),
                anyInt()))
                .thenReturn(Optional.of(card2))
                .thenReturn(Optional.of(card3));

        when(playerHandRepository.getHandValue(player1.getId())).thenReturn(19);
        when(playerHandRepository.getHandValue(player2.getId())).thenReturn(17);

        List<GameMessage> messages = gameService.handleDealerTurn(game);

        assertEquals(4, messages.size());
        assertEquals(GameState.NEW, game.getState());
        assertEquals(1, game.getCardOrder());
        assertEquals(54, game.getPlayer1Balance());
        assertEquals(60, game.getPlayer3Balance());
        assertEquals(86, game.getDealerBalance());
        assertEquals(25, game.getRemainingCards());
        CardDTO dto1 = new CardDTO(2, "card1.png");
        CardDTO dto2 = new CardDTO(9, "card2.png");
        CardDTO dto3 = new CardDTO(7, "card3.png");
        DealerHandDTO expected = new DealerHandDTO(List.of(dto1, dto2, dto3), 18);
        assertEquals(expected, messages.getLast().getDealerPublicHand());
        assertEquals("JOHN won 2 $!DEALER won 3 $ from JANE!", game.getInformation());
        assertEquals(6, player1.getGames());
        assertEquals(8, player2.getGames());
        assertEquals(4, player1.getWins());
        assertEquals(6, player2.getLosses());
        assertEquals(0, player1.getPot());
        assertEquals(0, player2.getPot());
    }

    @Test
    void handleDealerTurn_shouldDiscardAceAfterAnnouncingOhneAce_when_threeActivePlayers() {

        //handleDealerTurn
        Game game = new Game();
        game.setPlayer1("John");
        game.setPlayer2("Jack");
        game.setPlayer3("Jane");
        game.setDealerId(8L);
        game.setGameId(5L);
        game.setCardOrder(6);
        game.setDealerBalance(80);
        game.setState(GameState.IN_PROGRESS);
        game.setCardOrder(6);
        game.setPlayer1Balance(50);
        game.setPlayer2Balance(70);
        game.setPlayer3Balance(60);
        game.setRemainingCards(27);
        when(gameRepository.findById(game.getGameId())).thenReturn(Optional.of(game));

        //getPlayerWithActiveHands
        //addPlayerWithActiveHand
        Player player1 = new Player();
        player1.setPlayerName(game.getPlayer1());
        player1.setPlayerState(PlayerState.ENOUGH);
        player1.setId(3L);
        player1.setPot(4);
        player1.setGames(5);
        player1.setWins(3);
        player1.setLosses(2);
        player1.setBalance(game.getPlayer1Balance());
        when(playerRepository.findByPlayerName(player1.getPlayerName())).thenReturn(Optional.of(player1));

        Player player2 = new Player();
        player2.setPlayerName(game.getPlayer2());
        player2.setPlayerState(PlayerState.ENOUGH);
        player2.setId(4L);
        player2.setPot(6);
        player2.setGames(7);
        player2.setWins(2);
        player2.setLosses(5);
        player2.setBalance(game.getPlayer2Balance());
        when(playerRepository.findByPlayerName(player2.getPlayerName())).thenReturn(Optional.of(player2));


        Player player3 = new Player();
        player3.setPlayerName(game.getPlayer3());
        player3.setPlayerState(PlayerState.ENOUGH);
        player3.setId(5L);
        player3.setPot(8);
        player3.setGames(9);
        player3.setWins(4);
        player3.setLosses(5);
        player3.setBalance(game.getPlayer3Balance());
        when(playerRepository.findByPlayerName(player3.getPlayerName())).thenReturn(Optional.of(player3));

        //processWithActivePlayerHands
        Dealer dealer = new Dealer();
        dealer.setId(game.getDealerId());
        dealer.setBalance(game.getDealerBalance());
        when(dealerRepository.findById(game.getDealerId())).thenReturn(Optional.of(dealer));

        //getDealerHand először, az első lapot adja vissza
        DealerHand dealerCard1 = new DealerHand();
        dealerCard1.setCardValue(2);
        dealerCard1.setFrontImagePath("card1.png");
        dealerCard1.setDealer(dealer);


        //addMessageWithFirsCard
        GameMessage gameMessage = new GameMessage();
        when(messageService.gameToMessage(game)).thenReturn(gameMessage);

        //getMinHandValue
        //dealerPullsCards
        //automaticCardPulling
        Card card2 = new Card(); //ez a második lap
        card2.setValue(9);
        card2.setFrontImagePath("card2.png");
        //addNormalDealerMessage
        //getDealerHand másodszor, a második kártyát adja vissza
        DealerHand dealerCard2 = new DealerHand();
        dealerCard2.setCardValue(9);
        dealerCard2.setFrontImagePath("card2.png");
        dealerCard2.setDealer(dealer);

        //automaticCardPulling
        Card card3 = new Card(); //ez a harmadik lap
        card3.setValue(11);
        card3.setFrontImagePath("card3.png");

        //addNormalDealerMessage
        //getDealerHand harmadszor, a harmadik kártyát adja vissza
        DealerHand dealerCard3 = new DealerHand();
        dealerCard3.setCardValue(11);
        dealerCard3.setFrontImagePath("card3.png");
        dealerCard3.setDealer(dealer);

        //automaticCardPulling
        Card card4 = new Card(); //ez a harmadik lap
        card4.setValue(7);
        card4.setFrontImagePath("card4.png");

        //addNormalDealerMessage
        //getDealerHand harmadszor, a harmadik kártyát adja vissza
        DealerHand dealerCard4 = new DealerHand();
        dealerCard4.setCardValue(7);
        dealerCard4.setFrontImagePath("card4.png");
        dealerCard4.setDealer(dealer);

        when(dealerHandRepository.findAllByDealerId(dealer.getId()))
                .thenReturn(Optional.of(List.of(dealerCard1)))
                .thenReturn(Optional.of(List.of(dealerCard1, dealerCard2)))
                .thenReturn(Optional.of(List.of(dealerCard1, dealerCard2, dealerCard3)))
                .thenReturn(Optional.of(List.of(dealerCard1, dealerCard2, dealerCard4)));
        when(dealerHandRepository.getHandValue(dealer.getId()))
                .thenReturn(2)
                .thenReturn(11)
                .thenReturn(22)
                .thenReturn(18);
        when(shuffleRepository.findCardByGameIdAndCardOrder(
                eq(game.getGameId()),
                anyInt()))
                .thenReturn(Optional.of(card2))
                .thenReturn(Optional.of(card3))
                .thenReturn(Optional.of(card4));

        when(playerHandRepository.getHandValue(player1.getId())).thenReturn(19);
        when(playerHandRepository.getHandValue(player2.getId())).thenReturn(17);
        when(playerHandRepository.getHandValue(player3.getId())).thenReturn(20);

        List<GameMessage> messages = gameService.handleDealerTurn(game);

        assertEquals(6, messages.size());
        assertEquals(GameState.NEW, game.getState());
        assertEquals(1, game.getCardOrder());
        assertEquals(54, game.getPlayer1Balance());
        assertEquals(70, game.getPlayer2Balance());
        assertEquals(68, game.getPlayer3Balance());
        assertEquals(86, game.getDealerBalance());
        assertEquals(24, game.getRemainingCards());
        CardDTO dto1 = new CardDTO(2, "card1.png");
        CardDTO dto2 = new CardDTO(9, "card2.png");
        CardDTO dto3 = new CardDTO(7, "card4.png");
        DealerHandDTO expected = new DealerHandDTO(List.of(dto1, dto2, dto3), 18);
        assertEquals(expected, messages.getLast().getDealerPublicHand());
        assertEquals("JOHN won 2 $!DEALER won 3 $ from JACK!JANE won 4 $!", game.getInformation());
        assertEquals(6, player1.getGames());
        assertEquals(8, player2.getGames());
        assertEquals(10, player3.getGames());
        assertEquals(4, player1.getWins());
        assertEquals(6, player2.getLosses());
        assertEquals(5, player3.getWins());
        assertEquals(0, player1.getPot());
        assertEquals(0, player2.getPot());
        assertEquals(0, player3.getPot());
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
    void leaveGame_shouldReturnNull_when_lastPlayerLeaveGame() {
        Long gameId = 2L;
        String playerName = "John";
        Player player = new Player();
        player.setId(5L);
        player.setPlayerName(playerName);
        player.setCardNumber(3);
        player.setPlayerState(PlayerState.MUCH);
        when(playerRepository.findByPlayerName(playerName)).thenReturn(Optional.of(player));
        Game game = new Game();
        game.setGameId(gameId);
        game.setPlayer2(playerName);
        game.setDealerId(8L);
        when(gameRepository.findById(game.getGameId())).thenReturn(Optional.of(game));

        gameService.leaveGame(gameId, playerName);

        ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository, times(1)).save(playerCaptor.capture());
        Player capturedPlayer = playerCaptor.getValue();
        assertEquals(0, capturedPlayer.getCardNumber());
        assertEquals(PlayerState.WAITING_CARD, capturedPlayer.getPlayerState());
        verify(shuffleRepository, times(1)).deleteByGameId(gameId);
        verify(dealerHandRepository, times(1)).deleteAllByDealerId(game.getDealerId());
        verify(dealerRepository, times(1)).deleteById(game.getDealerId());
        verify(gameRepository, times(1)).deleteById(gameId);
    }

    @Test
    void leaveGame_shouldModifyGame_when_onePlayerRemainsAndPlayerNameDoesNotEqualTurnName() {
        Long gameId = 2L;
        String playerName = "John";
        Player player = new Player();
        player.setId(5L);
        player.setPlayerName(playerName);
        player.setCardNumber(3);
        player.setPlayerState(PlayerState.COULD_STOP);
        player.setPot(2);
        when(playerRepository.findByPlayerName(playerName)).thenReturn(Optional.of(player));
        Game game = new Game();
        game.setGameId(gameId);
        game.setPlayer1(playerName);
        game.setPlayer3("Jane");
        game.setDealerId(8L);
        game.setTurnName("Jane");
        game.setDealerBalance(45);
        game.setPlayer1Balance(55);
        when(gameRepository.findById(game.getGameId())).thenReturn(Optional.of(game));
        GameMessage gameMessage = new GameMessage();
        when(messageService.gameToMessage(game)).thenReturn(gameMessage);

        gameService.leaveGame(gameId, playerName);

        ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository, times(1)).save(playerCaptor.capture());
        Player capturedPlayer = playerCaptor.getValue();
        assertEquals(0, capturedPlayer.getCardNumber());
        assertEquals(PlayerState.WAITING_CARD, capturedPlayer.getPlayerState());
        assertEquals(0, capturedPlayer.getPot());

        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository, times(1)).save(gameCaptor.capture());
        Game capturedGame = gameCaptor.getValue();
        assertNull(capturedGame.getPlayer1());
        assertEquals("JOHN left the game!", capturedGame.getInformation());
        assertEquals(47, capturedGame.getDealerBalance());
        assertEquals(0, capturedGame.getPlayer1Balance());
        verify(playerHandRepository, times(1)).deleteAllByPlayerId(player.getId());
        verify(dealerRepository, times(1)).setDealerBalanceById(2, game.getDealerId());
    }

    @Test
    void leaveGame_shouldModifyGame_when_OnePlayerRemainsAndPlayerNameEqualsTurnName() {
        Long gameId = 2L;
        String playerName = "John";
        Player player = new Player();
        player.setId(5L);
        player.setPlayerName(playerName);
        player.setCardNumber(3);
        player.setPlayerState(PlayerState.FIRE);
        when(playerRepository.findByPlayerName(playerName)).thenReturn(Optional.of(player));
        Game game = new Game();
        game.setGameId(gameId);
        game.setPlayer3(playerName);
        game.setPlayer3Balance(55);
        game.setPlayer4("Jane");
        game.setDealerId(8L);
        game.setTurnName(playerName);
        game.setPublicHand3Exists(true);
        when(gameRepository.findById(game.getGameId())).thenReturn(Optional.of(game));
        when(playerRepository.getPlayerStateByPlayerName("Jane")).thenReturn(PlayerState.WAITING_CARD);
        GameMessage gameMessage = new GameMessage();
        when(messageService.gameToMessage(game)).thenReturn(gameMessage);

        gameService.leaveGame(gameId, playerName);

        verify(playerHandRepository, times(1)).deleteAllByPlayerId(player.getId());
        ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository, times(1)).save(playerCaptor.capture());
        Player capturedPlayer = playerCaptor.getValue();
        assertEquals(0, capturedPlayer.getCardNumber());
        assertEquals(PlayerState.WAITING_CARD, capturedPlayer.getPlayerState());

        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository, times(2)).save(gameCaptor.capture());
        Game capturedGame = gameCaptor.getValue();
        assertNull(capturedGame.getPlayer3());
        assertEquals(0, capturedGame.getPlayer3Balance());
        assertEquals("JOHN left the game!", capturedGame.getInformation());
        assertFalse(capturedGame.isPublicHand3Exists());
        assertEquals("Jane", capturedGame.getTurnName());
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
