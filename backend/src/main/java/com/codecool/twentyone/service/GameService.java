package com.codecool.twentyone.service;

import com.codecool.twentyone.model.dto.*;
import com.codecool.twentyone.model.dto.websocketdto.CardDTO;
import com.codecool.twentyone.model.dto.websocketdto.DealerHandDTO;
import com.codecool.twentyone.model.dto.websocketdto.PlayerHandDTO;
import com.codecool.twentyone.model.dto.websocketdto.PlayerStateDTO;
import com.codecool.twentyone.model.entities.*;
import com.codecool.twentyone.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final PlayerHandRepository playerHandRepository;
    private final DealerHandRepository dealerHandRepository;
    private final DealerRepository dealerRepository;
    private final ShuffleRepository shuffleRepository;
    private final MessageService messageService;

    public GameService(GameRepository gameRepository, PlayerRepository playerRepository, PlayerHandRepository playerHandRepository, DealerHandRepository dealerHandRepository, DealerRepository dealerRepository, ShuffleRepository shuffleRepository, MessageService messageService) {

        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.playerHandRepository = playerHandRepository;
        this.dealerHandRepository = dealerHandRepository;
        this.dealerRepository = dealerRepository;
        this.shuffleRepository = shuffleRepository;
        this.messageService = messageService;
    }

    @Transactional
    public PlayerHandDTO getFirstCard(Long gameId, String playerName) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        Card firstCard = shuffleRepository.findCardByGameIdAndCardOrder(gameId, game.getCardOrder()).orElseThrow(() -> new RuntimeException("Card not found"));
        Player player = playerRepository.findByPlayerName(playerName).orElseThrow(() -> new RuntimeException("Player not found"));
        PlayerCard playerCard = new PlayerCard();
        playerCard.setCardValue(firstCard.getValue());
        playerCard.setFrontImagePath(firstCard.getFrontImagePath());
        playerCard.setPlayer(player);
        playerHandRepository.save(playerCard);
        game.setCardOrder(game.getCardOrder() + 1);
        gameRepository.save(game);
        int handValue = playerCard.getCardValue();
        player.setCardNumber(1);
        playerRepository.save(player);
        CardDTO dto = new CardDTO(firstCard.getValue(), firstCard.getFrontImagePath());
        return new PlayerHandDTO(PlayerState.WAITING_CARD, List.of(dto), handValue, "hand.firstUpdate");
    }

    //@Transactional
    public void giveDealerFirstCard(Long gameId, Long dealerId) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        Card card = shuffleRepository.findCardByGameIdAndCardOrder(gameId, game.getCardOrder()).orElseThrow(() -> new RuntimeException("Card not found"));
        Dealer dealer = dealerRepository.findById(dealerId).orElseThrow(() -> new RuntimeException("Dealer not found"));
        DealerCard dealerCard = new DealerCard();
        dealerCard.setCardValue(card.getValue());
        dealerCard.setFrontImagePath(card.getFrontImagePath());
        dealerCard.setDealer(dealer);
        dealerHandRepository.save(dealerCard);
        game.setCardOrder(game.getCardOrder() + 1);
        gameRepository.save(game);
        dealer.setCardNumber(1);
        dealerRepository.save(dealer);
    }

    @Transactional
    public GameMessage pullCard(Long gameId, String playerName) {
        Player currentPlayer = loadPlayer(playerName);
        Game currentGame = loadGame(gameId);
        Dealer dealer = loadDealer(currentGame.getDealerId());
        dealCard(currentPlayer, currentGame);
        int handValue = playerHandRepository.getHandValue(currentPlayer.getId());
        int cardsNumber = playerHandRepository.getHandSize(currentPlayer.getId());
        evaluateAfterCardDraw(currentPlayer,currentGame, dealer, handValue, cardsNumber);
        if (currentGame.isCallNextTurnRequired()) {
            setNextTurnName(currentGame, playerName);
            currentGame.setCallNextTurnRequired(false);
        }
        gameRepository.save(currentGame);
        playerRepository.save(currentPlayer);
        dealerRepository.save(dealer);
        return messageService.gameToMessage(currentGame);
    }

    private void dealCard(Player currentPlayer, Game currentGame) {
        currentPlayer.setCardNumber(currentPlayer.getCardNumber() + 1);
        //playerRepository.save(currentPlayer);
        currentGame.setRemainingCards(currentGame.getRemainingCards() - 1);
        //Card card = shuffleRepository.findCardByGameIdAndCardOrder(currentGame.getGameId(), currentGame.getCardOrder()).orElseThrow(() -> new RuntimeException("Card not found"));
        Card card = loadNextCard(currentGame.getGameId(), currentGame.getCardOrder());
        currentGame.setCardOrder(currentGame.getCardOrder() + 1);
        PlayerCard handCard = new PlayerCard();
        handCard.setCardValue(card.getValue());
        handCard.setFrontImagePath(card.getFrontImagePath());
        handCard.setPlayer(currentPlayer);
        playerHandRepository.save(handCard);
    }

    private void evaluateAfterCardDraw(Player currentPlayer, Game currentGame, Dealer dealer, int handValue, int cardsNumber) {
        if (currentPlayer.getPlayerState().equals(PlayerState.OHNE_ACE) && handValue <= 14) {
            currentPlayer.setPlayerState(PlayerState.WAITING_CARD);
        } else if (handValue > 14 && handValue < 20) {
            currentPlayer.setPlayerState(PlayerState.COULD_STOP);
        } else if (handValue > 19 && handValue < 22) {
            handleStand(currentPlayer, currentGame);
        } else if (handValue == 22 && cardsNumber == 2) {
            handleFire(currentPlayer, currentGame);
        } else if (handValue >= 22 && !currentPlayer.getPlayerState().equals(PlayerState.OHNE_ACE)) {
            handleBust(currentPlayer, currentGame, dealer);
        }
    }

    private void handleStand(Player currentPlayer, Game currentGame) {
        currentPlayer.setPlayerState(PlayerState.ENOUGH);
        currentGame.setInformation((currentPlayer.getPlayerName().toUpperCase() + " announced 'stand'"));
        //setNextTurnName(currentGame, currentPlayer.getPlayerName());
        currentGame.setCallNextTurnRequired(true);
    }

    private void handleFire(Player currentPlayer, Game currentGame) {
        currentPlayer.setPlayerState(PlayerState.FIRE);
        currentPlayer.setBalance(currentPlayer.getBalance() + currentPlayer.getPot());
        currentGame.setInformation((currentPlayer.getPlayerName().toUpperCase() + " with aces-only hand won " + currentPlayer.getPot() / 2 + " $!"));
        //setNextTurnName(currentGame, currentPlayer.getPlayerName());
        currentGame.setCallNextTurnRequired(true);
        currentPlayer.setPot(0);
        currentPlayer.setGames(currentPlayer.getGames() + 1);
        currentPlayer.setWins(currentPlayer.getWins() + 1);
        setPublicHandWhenFire(currentPlayer, currentGame);
    }

    private void handleBust(Player currentPlayer, Game currentGame, Dealer dealer) {
        currentPlayer.setPlayerState(PlayerState.MUCH);
        dealer.setBalance(dealer.getBalance() + currentPlayer.getPot());
        currentGame.setDealerBalance(dealer.getBalance());
        currentGame.setInformation(currentPlayer.getPlayerName().toUpperCase() + " busted and lost " + currentPlayer.getPot() / 2 + " $!");
        //setNextTurnName(currentGame, currentPlayer.getPlayerName());
        currentGame.setCallNextTurnRequired(true);
        currentPlayer.setPot(0);
        currentPlayer.setGames(currentPlayer.getGames() + 1);
        currentPlayer.setLosses(currentPlayer.getLosses() + 1);
        setPublicHandWhenBust(currentPlayer, currentGame);
    }

    private void setPublicHandWhenFire(Player currentPlayer, Game currentGame) {
        if (currentGame.getPlayer1() != null && currentGame.getPlayer1().equals(currentPlayer.getPlayerName())) {
            currentGame.setPlayer1Balance(currentPlayer.getBalance());
            currentGame.setPublicHand1Exists(true);
        } else if (currentGame.getPlayer2() != null && currentGame.getPlayer2().equals(currentPlayer.getPlayerName())) {
            currentGame.setPlayer2Balance(currentPlayer.getBalance());
            currentGame.setPublicHand2Exists(true);
        } else if (currentGame.getPlayer3() != null && currentGame.getPlayer3().equals(currentPlayer.getPlayerName())) {
            currentGame.setPlayer3Balance(currentPlayer.getBalance());
            currentGame.setPublicHand3Exists(true);
        } else if (currentGame.getPlayer4() != null && currentGame.getPlayer4().equals(currentPlayer.getPlayerName())) {
            currentGame.setPlayer4Balance(currentPlayer.getBalance());
            currentGame.setPublicHand4Exists(true);
        }
    }

    private void setPublicHandWhenBust(Player currentPlayer, Game currentGame) {
        if (currentGame.getPlayer1() != null && currentGame.getPlayer1().equals(currentPlayer.getPlayerName())) {
            currentGame.setPublicHand1Exists(true);
        } else if (currentGame.getPlayer2() != null && currentGame.getPlayer2().equals(currentPlayer.getPlayerName())) {
            currentGame.setPublicHand2Exists(true);
        } else if (currentGame.getPlayer3() != null && currentGame.getPlayer3().equals(currentPlayer.getPlayerName())) {
            currentGame.setPublicHand3Exists(true);
        } else if (currentGame.getPlayer4() != null && currentGame.getPlayer4().equals(currentPlayer.getPlayerName())) {
            currentGame.setPublicHand4Exists(true);
        }
    }

    private Player loadPlayer(String playerName) {
        return playerRepository.findByPlayerName(playerName).orElseThrow(() -> new RuntimeException("Player not found"));
    }

    private Game loadGame(Long gameId) {
        return gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
    }

    private Dealer loadDealer(Long dealerId) {
        return dealerRepository.findById(dealerId).orElseThrow(() -> new RuntimeException("Dealer not found"));
    }

    private Card loadNextCard(Long gameId, int cardOrder) {
        return shuffleRepository.findCardByGameIdAndCardOrder(gameId, cardOrder).orElseThrow(() -> new RuntimeException("Card not found"));
    }

    public PlayerStateDTO getPlayerState(String playerName) {
        PlayerState state = playerRepository.getPlayerStateByPlayerName(playerName);
        return new PlayerStateDTO(state.toString(), "playerState.update");
    }

    private GameMessage passTurn(Game currentGame, String turnPlayerName) {
        setNextTurnName(currentGame, turnPlayerName);
        gameRepository.save(currentGame);
        return messageService.gameToMessage(currentGame);
    }

    public GameMessage passTurnWhenStand(Long gameId, String turnPlayerName) {
        Player currentPlayer = playerRepository.findByPlayerName(turnPlayerName).orElseThrow(() -> new RuntimeException("Player not found"));
        int handValue = playerHandRepository.getHandValue(currentPlayer.getId());
        if (handValue < 15) {
            throw new RuntimeException("You cannot stop under 15");
        } else if (currentPlayer.getPlayerState().equals(PlayerState.WAITING_CARD)) {
            throw new RuntimeException("You must take another card after placing a bet");
        }
        currentPlayer.setPlayerState(PlayerState.ENOUGH);
        playerRepository.save(currentPlayer);
        Game currentGame = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        currentGame.setInformation(currentPlayer.getPlayerName().toUpperCase() + " announced 'stand'");
        return passTurn(currentGame, turnPlayerName);
    }

    public PlayerHandDTO getHand(String playerName) {
        Player player = playerRepository.findByPlayerName(playerName).orElseThrow(() -> new RuntimeException("Player not found"));
        List<PlayerCard> ownCards = playerHandRepository.findAllByPlayerId(player.getId()).orElseThrow(() -> new RuntimeException("Cards not found"));
        List<CardDTO> cardDTOList = new ArrayList<>();
        int handValue = 0;
        for (PlayerCard card : ownCards) {
            cardDTOList.add(new CardDTO(card.getCardValue(), card.getFrontImagePath()));
            handValue += card.getCardValue();
        }
        //int handValue = playerHandRepository.getHandValue(player.getId());
        if (!player.getPlayerState().equals(PlayerState.OHNE_ACE)) {
            return new PlayerHandDTO(player.getPlayerState(), cardDTOList, handValue, "hand.update");
        }
        return new PlayerHandDTO(player.getPlayerState(), cardDTOList, handValue, "hand.withOhneAce");
    }

    private void setNextTurnName(Game currentGame, String turnPlayerName) {
        String[] players = {currentGame.getPlayer1(), currentGame.getPlayer2(), currentGame.getPlayer3(), currentGame.getPlayer4(), currentGame.getPlayer1(), currentGame.getPlayer2(), currentGame.getPlayer3()};
        for (int i = 0; i < 4; i++) {
            if (players[i] != null && players[i].equals(turnPlayerName)) {
                if (players[i + 1] != null && playerRepository.getPlayerStateByPlayerName(players[i + 1]).equals(PlayerState.WAITING_CARD)) {
                    currentGame.setTurnName(players[i + 1]);
                } else if (players[i + 2] != null && playerRepository.getPlayerStateByPlayerName(players[i + 2]).equals(PlayerState.WAITING_CARD)) {
                    currentGame.setTurnName(players[i + 2]);
                } else if (players[i + 3] != null && playerRepository.getPlayerStateByPlayerName(players[i + 3]).equals(PlayerState.WAITING_CARD)) {
                    currentGame.setTurnName(players[i + 3]);
                } else {
                    currentGame.setTurnName("Dealer");
                }
            }
        }
    }

    @Transactional
    public List<GameMessage> handleDealerTurn(Game currentGame) {
        List<GameMessage> messages = new ArrayList<>();
        List<Player> playersWithActiveHands = getPlayersWithActiveHands(currentGame);
        if (playersWithActiveHands.isEmpty()) {
            processWithoutActivePlayerHand(currentGame, messages);
        } else {
            processWithActivePlayerHands(currentGame, messages, playersWithActiveHands);
        }
        return messages;
    }

    private List<Player> getPlayersWithActiveHands(Game currentGame) {
        List<Player> playersWithActiveHands = new ArrayList<>();
        addPlayerWithActiveHand(PlayerSlot.PLAYER1, currentGame, playersWithActiveHands);
        addPlayerWithActiveHand(PlayerSlot.PLAYER2, currentGame, playersWithActiveHands);
        addPlayerWithActiveHand(PlayerSlot.PLAYER3, currentGame, playersWithActiveHands);
        addPlayerWithActiveHand(PlayerSlot.PLAYER4, currentGame, playersWithActiveHands);
        return playersWithActiveHands;
    }

    private void addPlayerWithActiveHand(PlayerSlot slot, Game currentGame, List<Player> playersWithActiveHands) {
        String playerName = slot.getPlayer.apply(currentGame);
        if (playerName != null) {
            Player player = playerRepository.findByPlayerName(playerName).orElseThrow(() -> new RuntimeException("Player not found"));
            if (player.getPlayerState().equals(PlayerState.ENOUGH)) {
                playersWithActiveHands.add(player);
                slot.setPublicHandExists.accept(currentGame, true);
            }
        }
    }

    private void processWithoutActivePlayerHand(Game currentGame, List<GameMessage> messages) {
        currentGame.setState(GameState.NEW);
        currentGame.setCardOrder(1);
        GameMessage messageWithoutActiveHand = messageService.gameToMessage(currentGame);
        DealerHandDTO firstCardDTO = getDealerHand(currentGame.getGameId());
        messageWithoutActiveHand.setDealerPublicHand(firstCardDTO);
        messages.add(messageWithoutActiveHand);
    }

    private void processWithActivePlayerHands(Game currentGame, List<GameMessage> messages, List<Player> playersWithActiveHands) {
        Dealer dealer = dealerRepository.findById(currentGame.getDealerId()).orElseThrow(() -> new RuntimeException("Dealer not found"));
        DealerHandDTO firstCardDTO = getDealerHand(currentGame.getGameId());
        addMessageWithFirstCard(currentGame, messages, firstCardDTO);
        int dealerHandValue = firstCardDTO.handValue();
        //int dealerHandValue = dealerHandRepository.getHandValue(currentGame.getDealerId()); //a DealerHandDTO-ban benne van
        boolean announcedOhneAce = false;
        int dealerCardsNumber = 1;
        int minDealerHandValue = getMinDealerHandValue(playersWithActiveHands.size());
        dealerHandValue = dealerPullsCards(currentGame, dealerHandValue, minDealerHandValue, dealer, dealerCardsNumber, messages, announcedOhneAce);
        compareActiveHandsWithDealerHand(currentGame, playersWithActiveHands, dealerHandValue, dealer);

        dealerRepository.save(dealer);
        currentGame.setDealerBalance(dealer.getBalance());
        currentGame.setState(GameState.NEW);
        currentGame.setCardOrder(1);
        addLastDealerMessage(currentGame, messages);

        gameRepository.save(currentGame);
    }

    private void addMessageWithFirstCard(Game currentGame, List<GameMessage> messages, DealerHandDTO firstCardDTO) {
        GameMessage firstMessage = messageService.gameToMessage(currentGame);
        firstMessage.setDealerPublicHand(firstCardDTO);
        messages.add(firstMessage);
    }

    private int getMinDealerHandValue(int activePlayers) {
        if (activePlayers < 3) {
            return 15;
        } else {
            return 16;
        }
    }

    private int dealerPullsCards(Game currentGame, int dealerHandValue, int minDealerHandValue, Dealer dealer, int dealerCardsNumber, List<GameMessage> messages, boolean announcedOhneAce) {
        while (dealerHandValue < minDealerHandValue) {
            dealerHandValue = automaticCardPulling(currentGame, dealerHandValue, dealer);
            dealerCardsNumber++;
            if (dealerCardsNumber == 5 && dealerHandValue < 17) {
                dealerDiscardsFiveCards(currentGame, messages, dealer);
                dealerHandValue = 0;
                dealerCardsNumber = 0;
            } else if (dealerHandValue == 11 && !announcedOhneAce && dealerCardsNumber > 1) {
                announcedOhneAce = true;
                dealerAnnouncesOhneAce(currentGame, messages);
            } else if (announcedOhneAce && dealerHandValue == 22) {
                dealerDiscardsAce(currentGame, messages);
                dealerHandValue = 11;
                dealerCardsNumber--;
            } else {
                if (announcedOhneAce && dealerHandValue < 22) {
                    announcedOhneAce = false;
                }
                addNormalDealerMessage(currentGame, messages);
            }
        }
        return dealerHandValue;
    }

    private int automaticCardPulling(Game currentGame, int dealerHandValue, Dealer dealer) {
        Card newCard = shuffleRepository.findCardByGameIdAndCardOrder(currentGame.getGameId(), currentGame.getCardOrder()).orElseThrow(() -> new RuntimeException("Card not found"));
        currentGame.setCardOrder(currentGame.getCardOrder() + 1);
        dealerHandValue = dealerHandValue + newCard.getValue();
        DealerCard dealerCard = new DealerCard();
        dealerCard.setCardValue(newCard.getValue());
        dealerCard.setFrontImagePath(newCard.getFrontImagePath());
        dealerCard.setDealer(dealer);
        dealerHandRepository.save(dealerCard);
        currentGame.setRemainingCards(currentGame.getRemainingCards() - 1);
        return dealerHandValue;
    }

    private void dealerDiscardsFiveCards(Game currentGame, List<GameMessage> messages, Dealer dealer) {
        GameMessage gameMessage = messageService.gameToMessage(currentGame);
        DealerHandDTO dealerHandDTO = getDealerHand(currentGame.getGameId());
        gameMessage.setDealerPublicHand(dealerHandDTO);
        messages.add(gameMessage);
        currentGame.setInformation("Dealer discarded 5 cards!");
        GameMessage messageWithEmptyHand = messageService.gameToMessage(currentGame);
        currentGame.setInformation(null);
        dealerHandRepository.deleteAllByDealerId(dealer.getId());
        DealerHandDTO emptyDTO = new DealerHandDTO(List.of(), 0);
        messageWithEmptyHand.setDealerPublicHand(emptyDTO);
        messages.add(messageWithEmptyHand);
    }

    private void dealerAnnouncesOhneAce(Game currentGame, List<GameMessage> messages) {
        currentGame.setInformation("Dealer announced 'Ohne Ace'");
        GameMessage gameMessage = messageService.gameToMessage(currentGame);
        currentGame.setInformation(null);
        DealerHandDTO handDTO = getDealerHand(currentGame.getGameId());
        gameMessage.setDealerPublicHand(handDTO);
        messages.add(gameMessage);
    }

    private void dealerDiscardsAce(Game currentGame, List<GameMessage> messages) {
        GameMessage gameMessage = messageService.gameToMessage(currentGame);
        DealerHandDTO handDTO = getDealerHand(currentGame.getGameId());
        gameMessage.setDealerPublicHand(handDTO);
        messages.add(gameMessage);
        GameMessage messageWithoutAce = messageService.gameToMessage(currentGame);
        messageWithoutAce.setContent("Dealer discarded an Ace after announcing 'Ohne Ace'");
        dealerHandRepository.deleteAceByDealerId(currentGame.getDealerId());
        DealerHandDTO handDTOWithoutAce = getDealerHand(currentGame.getGameId());
        messageWithoutAce.setDealerPublicHand(handDTOWithoutAce);
        messages.add(messageWithoutAce);
    }

    private void addNormalDealerMessage(Game currentGame, List<GameMessage> messages) {
        GameMessage gameMessage = messageService.gameToMessage(currentGame);
        DealerHandDTO handDTO = getDealerHand(currentGame.getGameId());
        gameMessage.setDealerPublicHand(handDTO);
        messages.add(gameMessage);
    }

    private void compareActiveHandsWithDealerHand(Game currentGame, List<Player> playersWithActiveHands, int dealerHandValue, Dealer dealer) {
        String roundResult = "";
        for (Player player : playersWithActiveHands) {
            if (playerHandRepository.getHandValue(player.getId()) > dealerHandValue || dealerHandValue > 22 || (dealerHandValue == 22 && dealerHandRepository.getHandSize(currentGame.getDealerId()) > 2)) {
                roundResult = processWhenPlayerWon(currentGame, player, roundResult);
            } else {
                roundResult = processWhenDealerWon(player, dealer, roundResult);
            }
            player.setPot(0);
            playerRepository.save(player);
            currentGame.setInformation(roundResult);
        }
    }

    private String processWhenPlayerWon(Game currentGame, Player player, String roundResult) {
        player.setBalance(player.getBalance() + player.getPot());
        player.setGames(player.getGames() + 1);
        player.setWins(player.getWins() + 1);
        if (player.getPlayerName().equals(currentGame.getPlayer1())) {
            currentGame.setPlayer1Balance(player.getBalance());
            roundResult += currentGame.getPlayer1().toUpperCase() + " won " + player.getPot() / 2 + " $!";
        } else if (player.getPlayerName().equals(currentGame.getPlayer2())) {
            currentGame.setPlayer2Balance(player.getBalance());
            roundResult += currentGame.getPlayer2().toUpperCase() + " won " + player.getPot() / 2 + " $!";
        } else if (player.getPlayerName().equals(currentGame.getPlayer3())) {
            currentGame.setPlayer3Balance(player.getBalance());
            roundResult += currentGame.getPlayer3().toUpperCase() + " won " + player.getPot() / 2 + " $!";
        } else {
            currentGame.setPlayer4Balance(player.getBalance());
            roundResult += currentGame.getPlayer4().toUpperCase() + " won " + player.getPot() / 2 + " $!";
        }
        return roundResult;
    }

    private String processWhenDealerWon(Player player, Dealer dealer, String roundResult) {
        dealer.setBalance(dealer.getBalance() + player.getPot());
        roundResult += "DEALER won " + player.getPot() / 2 + " $ from " + player.getPlayerName().toUpperCase()+"!";
        player.setGames(player.getGames() + 1);
        player.setLosses(player.getLosses() + 1);
        return roundResult;
    }

    private void addLastDealerMessage(Game currentGame, List<GameMessage> messages) {
        GameMessage finalMessage = messageService.gameToMessage(currentGame);
        DealerHandDTO finalHandDTO = getDealerHand(currentGame.getGameId());
        finalMessage.setDealerPublicHand(finalHandDTO);
        messages.add(finalMessage);
    }

    public DealerHandDTO getDealerHand(Long gameId) {
        Game currentGame = gameRepository.findById(gameId).orElseThrow(() -> new NoSuchElementException("Game not found"));
        List<DealerCard> dealerCards = dealerHandRepository.findAllByDealerId(currentGame.getDealerId()).orElseThrow(() -> new NoSuchElementException("Cards not found"));
        List<CardDTO> dealerCardDTOs = new ArrayList<>();
        for (DealerCard dealerCard : dealerCards) {
            CardDTO cardDTO = new CardDTO(dealerCard.getCardValue(), dealerCard.getFrontImagePath());
            dealerCardDTOs.add(cardDTO);
        }
        int dealerHandValue = dealerHandRepository.getHandValue(currentGame.getDealerId()); //dealerCards-ból nem gyorsabb?
        return new DealerHandDTO(dealerCardDTOs, dealerHandValue);
    }

    public GameMessage raiseBet(Long gameId, String turnName, int bet) {
        if (bet < 0) {
            throw new IllegalArgumentException("Bet cannot be negative");
        }
        Game currentGame = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        Dealer dealer = dealerRepository.findById(currentGame.getDealerId()).orElseThrow(() -> new RuntimeException("Dealer not found"));
        betProcessOfTurnPlayer(turnName, bet, currentGame, dealer);
        gameRepository.save(currentGame);
        return messageService.gameToMessage(currentGame);
    }

    private void betProcessOfTurnPlayer(String turnName, Integer bet, Game currentGame, Dealer dealer) {
        if (turnName.equals(currentGame.getPlayer1())) {
            placingBetProcess(currentGame, bet, dealer, currentGame.getPlayer1());
            currentGame.setPlayer1Balance(currentGame.getPlayer1Balance() - bet);
        } else if (turnName.equals(currentGame.getPlayer2())) {
            placingBetProcess(currentGame, bet, dealer, currentGame.getPlayer2());
            currentGame.setPlayer2Balance(currentGame.getPlayer2Balance() - bet);
        } else if (turnName.equals(currentGame.getPlayer3())) {
            placingBetProcess(currentGame, bet, dealer, currentGame.getPlayer3());
            currentGame.setPlayer3Balance(currentGame.getPlayer3Balance() - bet);
        } else {
            placingBetProcess(currentGame, bet, dealer, currentGame.getPlayer4());
            currentGame.setPlayer4Balance(currentGame.getPlayer4Balance() - bet);
        }
    }

    private void placingBetProcess(Game currentGame, int bet, Dealer dealer, String turnName) {
        Player player = playerRepository.findByPlayerName(turnName).orElseThrow(() -> new RuntimeException("Player not found"));
        if (player.getBalance() < bet) {
            throw new RuntimeException("Player's balance is less than bet");
        }
        if (dealer.getBalance() < bet) {
            throw new RuntimeException("Dealer balance is less than bet");
        }
        player.setBalance(player.getBalance() - bet);
        player.setPot(player.getPot() + bet * 2);
        if (!player.getPlayerState().equals(PlayerState.OHNE_ACE) && bet > 0) {
            player.setPlayerState(PlayerState.WAITING_CARD);
        }
        playerRepository.save(player);
        currentGame.setDealerBalance(currentGame.getDealerBalance() - bet);
        currentGame.setInformation(player.getPlayerName().toUpperCase() + " placed a " + bet + " $ bet!");
        dealer.setBalance(dealer.getBalance() - bet);
        dealerRepository.save(dealer);
    }

    @Transactional
    public PlayerStateDTO setPlayerStateToOhneAce(String playerName) {
        playerRepository.setPlayerStateByPlayerName(playerName);
        return new PlayerStateDTO((PlayerState.OHNE_ACE).toString(), "playerState.update");
    }

    public GameMessage setContent(Long gameId, String content) {
        Game currentGame = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        currentGame.setInformation(content);
        gameRepository.save(currentGame);
        return messageService.gameToMessage(currentGame);
    }

    @Transactional
    public PlayerHandDTO throwAce(String playerName) {
        Player player = playerRepository.findByPlayerName(playerName).orElseThrow(() -> new RuntimeException("Player not found"));
        playerHandRepository.deleteAceFromHand(player.getId());
        player.setCardNumber(player.getCardNumber() - 1);
        playerRepository.save(player);
        List<PlayerCard> cards = playerHandRepository.findAllByPlayerId(player.getId()).orElseThrow(() -> new RuntimeException("Cards not found"));
        List<CardDTO> dtos = new ArrayList<>();
        for (PlayerCard card : cards) {
            CardDTO cardDTO = new CardDTO(card.getCardValue(), card.getFrontImagePath());
            dtos.add(cardDTO);
        }
        return new PlayerHandDTO(PlayerState.WAITING_CARD, dtos, 11, "game.throwAce");
    }

    @Transactional
    public GameMessage leaveGame(Long gameId, String playerName) {

        //Személyes játékadatok törlése
        Player leavingPlayer = playerRepository.findByPlayerName(playerName).orElseThrow(() -> new RuntimeException("Player not found"));
        playerHandRepository.deleteAllByPlayerId(leavingPlayer.getId());
        leavingPlayer.setCardNumber(0);
        leavingPlayer.setPlayerState(PlayerState.WAITING_CARD);
        Game currentGame = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        if (leavingPlayer.getPot() > 0) {
            dealerRepository.setDealerBalanceById(leavingPlayer.getPot(), currentGame.getDealerId());
            currentGame.setDealerBalance(currentGame.getDealerBalance() + leavingPlayer.getPot());
            leavingPlayer.setPot(0);
        }
        playerRepository.save(leavingPlayer);

        //Eldöntjük, hogy egy vagy több játékos van-e játékban

        int playersNumber = countActivePlayers(currentGame);

        //Eljárás egy játékos esetén
        if (playersNumber == 1) {
            shuffleRepository.deleteByGameId(gameId);
            dealerHandRepository.deleteAllByDealerId(currentGame.getDealerId());
            dealerRepository.deleteById(currentGame.getDealerId());
            gameRepository.deleteById(gameId);
            return null;
        }

        //Eljárás több játékos esetén

        if (playerName.equals(currentGame.getPlayer1())) {
            return handlePlayerLeaving(currentGame, currentGame.getPlayer1(), PlayerSlot.PLAYER1);
        } else if (playerName.equals(currentGame.getPlayer2())) {
            return handlePlayerLeaving(currentGame, currentGame.getPlayer2(), PlayerSlot.PLAYER2);
        } else if (playerName.equals(currentGame.getPlayer3())) {
            return handlePlayerLeaving(currentGame, currentGame.getPlayer3(), PlayerSlot.PLAYER3);
        } else if (playerName.equals(currentGame.getPlayer4())) {
            return handlePlayerLeaving(currentGame, currentGame.getPlayer4(), PlayerSlot.PLAYER4);
        }
        return messageService.gameToMessage(currentGame);
    }

    private GameMessage handlePlayerLeaving(Game currentGame, String playerName, PlayerSlot slot) {
        slot.setBalance.accept(currentGame, 0);
        currentGame.setInformation(playerName.toUpperCase() + " left the game!");
        slot.setPublicHandExists.accept(currentGame, false);

        if (playerName.equals(currentGame.getTurnName())) {
            GameMessage message = passTurn(currentGame, playerName);
            slot.setPlayer.accept(currentGame, null);
            gameRepository.save(currentGame);
            message.setLeavingPlayer(slot.playerName);
            return message;
        }
        slot.setPlayer.accept(currentGame, null);
        gameRepository.save(currentGame);
        GameMessage leavingMessage = messageService.gameToMessage(currentGame);
        leavingMessage.setLeavingPlayer(slot.playerName);
        return leavingMessage;
    }

    private int countActivePlayers(Game currentGame) {
        int playersNumber = 0;
        if (currentGame.getPlayer1() != null) {
            playersNumber++;
        }
        if (currentGame.getPlayer2() != null) {
            playersNumber++;
        }
        if (currentGame.getPlayer3() != null) {
            playersNumber++;
        }
        if (currentGame.getPlayer4() != null) {
            playersNumber++;
        }
        return playersNumber;
    }


    @Transactional
    public GameMessage throwCards(String playerName, Long gameId) {
        Player playerWithFiveCards = playerRepository.findByPlayerName(playerName).orElseThrow(()-> new IllegalArgumentException("Player not found"));
        playerHandRepository.deleteAllByPlayerId(playerWithFiveCards.getId());
        playerWithFiveCards.setPlayerState(PlayerState.WAITING_CARD);
        playerWithFiveCards.setCardNumber(0);
        playerRepository.save(playerWithFiveCards);
        Game currentGame = gameRepository.findById(gameId).orElseThrow(()-> new RuntimeException("Game not found"));
        currentGame.setInformation(playerName.toUpperCase() + " discarded 5 cards!");
        gameRepository.save(currentGame);
        return messageService.gameToMessage(currentGame);
    }
}
