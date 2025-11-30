package com.codecool.twentyone.service;

import com.codecool.twentyone.model.dto.*;
import com.codecool.twentyone.model.entities.*;
import com.codecool.twentyone.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    public PlayerHandDTO getFirstCard(Long gameId, String playerName) {
        //shuffleService.addShuffledDeck(gameId);
        Card firstCard = shuffleRepository.findCardByGameIdAndCardOrder(gameId, cardOrder).orElseThrow(() -> new RuntimeException("Card not found"));
        Player player = playerRepository.findByPlayerName(playerName).orElseThrow(() -> new RuntimeException("Player not found"));
        PlayerHand hand = new PlayerHand();
        hand.setCardValue(firstCard.getValue());
        hand.setFrontImagePath(firstCard.getFrontImagePath());
        hand.setPlayer(player);
        playerHandRepository.save(hand);
        cardOrder++;
        int handValue = hand.getCardValue();
        player.setCardNumber(1);
        playerRepository.save(player);
        CardDTO dto = new CardDTO(firstCard.getValue(), firstCard.getFrontImagePath());
        return new PlayerHandDTO(PlayerState.WAITING_CARD, List.of(dto), handValue, "hand.firstUpdate");
    }

    public void giveDealerFirstCard(Long gameId, Long dealerId) {
        Card card = shuffleRepository.findCardByGameIdAndCardOrder(gameId, cardOrder).orElseThrow(() -> new RuntimeException("Card not found"));
        Dealer dealer = dealerRepository.findById(dealerId).orElseThrow(() -> new RuntimeException("Dealer not found"));
        DealerHand hand = new DealerHand();
        hand.setCardValue(card.getValue());
        hand.setFrontImagePath(card.getFrontImagePath());
        hand.setDealer(dealer);
        dealerHandRepository.save(hand);
        cardOrder++;
        dealer.setCardNumber(1);
        dealerRepository.save(dealer);
    }

    public GameMessage pullCard(Long gameId, String playerName) {
        Player currentPlayer = playerRepository.findByPlayerName(playerName).orElseThrow(() -> new RuntimeException("Player not found"));
        currentPlayer.setCardNumber(currentPlayer.getCardNumber() + 1);
        playerRepository.save(currentPlayer);
        Game currentGame = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        currentGame.setRemainingCards(currentGame.getRemainingCards() - 1);
        Dealer dealer = dealerRepository.findById(currentGame.getDealerId()).orElseThrow(() -> new RuntimeException("Dealer not found"));
        Card card = shuffleRepository.findCardByGameIdAndCardOrder(gameId, cardOrder).orElseThrow(() -> new RuntimeException("Card not found"));
        cardOrder++;
        PlayerHand handCard = new PlayerHand();
        handCard.setCardValue(card.getValue());
        handCard.setFrontImagePath(card.getFrontImagePath());
        handCard.setPlayer(currentPlayer);
        playerHandRepository.save(handCard);
        int handValue = playerHandRepository.getHandValue(currentPlayer.getId());
        int cardsNumber = playerHandRepository.getHandSize(currentPlayer.getId());
        if (handValue > 14 && handValue < 20) {
            currentPlayer.setPlayerState(PlayerState.COULD_STOP);
        } else if (handValue > 19 && handValue < 22) {
            currentPlayer.setPlayerState(PlayerState.ENOUGH);
            currentGame.setInformation((currentPlayer.getPlayerName().toUpperCase() + " has stood"));
        } else if (handValue == 22 && cardsNumber == 2) {
            currentPlayer.setPlayerState(PlayerState.FIRE);
            currentPlayer.setBalance(currentPlayer.getBalance() + currentPlayer.getPot());
            currentGame.setInformation((currentPlayer.getPlayerName().toUpperCase() + " with aces-only hand has won " + currentPlayer.getPot() / 2 + " $!"));
            currentPlayer.setPot(0);
            if (currentGame.getPlayer1().equals(playerName)) {
                currentGame.setPlayer1Balance(currentPlayer.getBalance());
                currentGame.setPublicHand1Exists(true);
            } else if (currentGame.getPlayer2().equals(playerName)) {
                currentGame.setPlayer2Balance(currentPlayer.getBalance());
                currentGame.setPublicHand2Exists(true);
            } else if (currentGame.getPlayer3().equals(playerName)) {
                currentGame.setPlayer3Balance(currentPlayer.getBalance());
                currentGame.setPublicHand1Exists(true);
            } else if (currentGame.getPlayer4().equals(playerName)) {
                currentGame.setPlayer4Balance(currentPlayer.getBalance());
                currentGame.setPublicHand4Exists(true);
            }

        } else if (handValue >= 22 && !currentPlayer.getPlayerState().equals(PlayerState.OHNE_ACE)) {
            currentPlayer.setPlayerState(PlayerState.MUCH);
            dealer.setBalance(dealer.getBalance() + currentPlayer.getPot());
            currentGame.setDealerBalance(dealer.getBalance());
            currentGame.setInformation(currentPlayer.getPlayerName().toUpperCase() + " has busted and lost " + currentPlayer.getPot() / 2 + " $!");
            currentPlayer.setPot(0);
            if (currentGame.getPlayer1().equals(playerName)) {
                currentGame.setPublicHand1Exists(true);
            } else if (currentGame.getPlayer2().equals(playerName)) {
                currentGame.setPublicHand2Exists(true);
            } else if (currentGame.getPlayer3().equals(playerName)) {
                currentGame.setPublicHand3Exists(true);
            } else if (currentGame.getPlayer4().equals(playerName)) {
                currentGame.setPublicHand4Exists(true);
            }
        }
        gameRepository.save(currentGame);
        playerRepository.save(currentPlayer);
        dealerRepository.save(dealer);
        return messageService.gameToMessage(currentGame);
    }

    public GameMessage passTurn(Long gameId, String turnPlayerName) {
        Player currentPlayer = playerRepository.findByPlayerName(turnPlayerName).orElseThrow(() -> new RuntimeException("Player not found"));
        int handValue = playerHandRepository.getHandValue(currentPlayer.getId());
        if (handValue < 15) {
            throw new RuntimeException("You cannot stop under 15");
        }
        currentPlayer.setPlayerState(PlayerState.ENOUGH);
        playerRepository.save(currentPlayer);
        Game currentGame = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        currentGame.setInformation(currentPlayer.getPlayerName().toUpperCase() + " has stood");
        String[] players = {currentGame.getPlayer1(), currentGame.getPlayer2(), currentGame.getPlayer3(), currentGame.getPlayer4(), currentGame.getPlayer1(), currentGame.getPlayer2(), currentGame.getPlayer3()};
        for (int i = 0; i < 4; i++) {
            if (players[i] != null && players[i].equals(turnPlayerName)) {
                if (players[i + 1] != null && playerRepository.getPlayerStateByPlayerName(players[i + 1]).equals(PlayerState.WAITING_CARD)) {
                    currentGame.setTurnName(players[i + 1]);
                    gameRepository.save(currentGame);
                    return messageService.gameToMessage(currentGame);
                } else if (players[i + 2] != null && playerRepository.getPlayerStateByPlayerName(players[i + 2]).equals(PlayerState.WAITING_CARD)) {
                    currentGame.setTurnName(players[i + 2]);
                    gameRepository.save(currentGame);
                    return messageService.gameToMessage(currentGame);
                } else if (players[i + 3] != null && playerRepository.getPlayerStateByPlayerName(players[i + 3]).equals(PlayerState.WAITING_CARD)) {
                    currentGame.setTurnName(players[i + 3]);
                    gameRepository.save(currentGame);
                    return messageService.gameToMessage(currentGame);
                } else {
                    currentGame.setTurnName("Dealer");
                    gameRepository.save(currentGame);
                    return messageService.gameToMessage(currentGame);
                }
            }
        }
        return null;
    }

    public PlayerHandDTO getHand(String playerName) {
        Player player = playerRepository.findByPlayerName(playerName).orElseThrow(() -> new RuntimeException("Player not found"));
        List<PlayerHand> ownCards = playerHandRepository.findAllByPlayerId(player.getId()).orElseThrow(() -> new RuntimeException("Cards not found"));
        List<CardDTO> cardDTOList = new ArrayList<>();
        for (PlayerHand card : ownCards) {
            cardDTOList.add(new CardDTO(card.getCardValue(), card.getFrontImagePath()));
        }
        int handValue = playerHandRepository.getHandValue(player.getId());
        if (!player.getPlayerState().equals(PlayerState.OHNE_ACE)) {
            return new PlayerHandDTO(player.getPlayerState(), cardDTOList, handValue, "hand.update");
        }
        return new PlayerHandDTO(player.getPlayerState(), cardDTOList, handValue, "hand.withOhneAce");
    }

    public PublicHandsDTO getPublicHandsByNewPlayer(Long gameId) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        List<PublicHandDTO> publicHandDTOs = new ArrayList<>();
        if (game.isPublicHand1Exists()) {
            Player player = playerRepository.findByPlayerName(game.getPlayer1()).orElseThrow(() -> new RuntimeException("Player not found"));
            List<PlayerHand> cards = playerHandRepository.findAllByPlayerId(player.getId()).orElseThrow(() -> new RuntimeException("Cards not found"));
            List<CardDTO> cardDTOList = new ArrayList<>();
            for (PlayerHand card : cards) {
                cardDTOList.add(new CardDTO(card.getCardValue(), card.getFrontImagePath()));
            }
            int handValue = playerHandRepository.getHandValue(player.getId());
            PublicHandDTO dto = new PublicHandDTO(cardDTOList, handValue);
            publicHandDTOs.add(dto);
        } else {
            PublicHandDTO dto = new PublicHandDTO(List.of(), 0);
            publicHandDTOs.add(dto);
        }
        if (game.isPublicHand2Exists()) {
            Player player = playerRepository.findByPlayerName(game.getPlayer2()).orElseThrow(() -> new RuntimeException("Player not found"));
            List<PlayerHand> cards = playerHandRepository.findAllByPlayerId(player.getId()).orElseThrow(() -> new RuntimeException("Cards not found"));
            List<CardDTO> cardDTOList = new ArrayList<>();
            for (PlayerHand card : cards) {
                cardDTOList.add(new CardDTO(card.getCardValue(), card.getFrontImagePath()));
            }
            int handValue = playerHandRepository.getHandValue(player.getId());
            PublicHandDTO dto = new PublicHandDTO(cardDTOList, handValue);
            publicHandDTOs.add(dto);
        } else {
            PublicHandDTO dto = new PublicHandDTO(List.of(), 0);
            publicHandDTOs.add(dto);
        }
        if (game.isPublicHand3Exists()) {
            Player player = playerRepository.findByPlayerName(game.getPlayer3()).orElseThrow(() -> new RuntimeException("Player not found"));
            List<PlayerHand> cards = playerHandRepository.findAllByPlayerId(player.getId()).orElseThrow(() -> new RuntimeException("Cards not found"));
            List<CardDTO> cardDTOList = new ArrayList<>();
            for (PlayerHand card : cards) {
                cardDTOList.add(new CardDTO(card.getCardValue(), card.getFrontImagePath()));
            }
            int handValue = playerHandRepository.getHandValue(player.getId());
            PublicHandDTO dto = new PublicHandDTO(cardDTOList, handValue);
            publicHandDTOs.add(dto);
        } else {
            PublicHandDTO dto = new PublicHandDTO(List.of(), 0);
            publicHandDTOs.add(dto);
        }
        if (game.isPublicHand4Exists()) {
            Player player = playerRepository.findByPlayerName(game.getPlayer4()).orElseThrow(() -> new RuntimeException("Player not found"));
            List<PlayerHand> cards = playerHandRepository.findAllByPlayerId(player.getId()).orElseThrow(() -> new RuntimeException("Cards not found"));
            List<CardDTO> cardDTOList = new ArrayList<>();
            for (PlayerHand card : cards) {
                cardDTOList.add(new CardDTO(card.getCardValue(), card.getFrontImagePath()));
            }
            int handValue = playerHandRepository.getHandValue(player.getId());
            PublicHandDTO dto = new PublicHandDTO(cardDTOList, handValue);
            publicHandDTOs.add(dto);
        } else {
            PublicHandDTO dto = new PublicHandDTO(List.of(), 0);
            publicHandDTOs.add(dto);
        }
        return new PublicHandsDTO(publicHandDTOs, "publicHands.update");
    }

    public PublicHandsDTO getPublicHands(Long gameId) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        Player player1;
        Player player2;
        Player player3;
        Player player4;
        List<CardDTO> player1Cards = new ArrayList<>();
        PublicHandDTO player1DTO = null;
        if (game.getPlayer1() != null) {
            player1 = playerRepository.findByPlayerName(game.getPlayer1()).orElseThrow(() -> new RuntimeException("Player not found"));
            List<PlayerHand> player1Hand = playerHandRepository.findAllByPlayerId(player1.getId()).orElseThrow();
            for (PlayerHand card : player1Hand) {
                CardDTO cardDTO = new CardDTO(card.getCardValue(), card.getFrontImagePath());
                player1Cards.add(cardDTO);
            }
            int handValue = playerHandRepository.getHandValue(player1.getId());
            player1DTO = new PublicHandDTO(player1Cards, handValue);
        }
        List<CardDTO> player2Cards = new ArrayList<>();
        PublicHandDTO player2DTO = null;
        if (game.getPlayer2() != null) {
            player2 = playerRepository.findByPlayerName(game.getPlayer2()).orElseThrow(() -> new RuntimeException("Player not found"));
            List<PlayerHand> player2Hand = playerHandRepository.findAllByPlayerId(player2.getId()).orElseThrow();
            for (PlayerHand card : player2Hand) {
                CardDTO cardDTO = new CardDTO(card.getCardValue(), card.getFrontImagePath());
                player2Cards.add(cardDTO);
            }
            int handValue = playerHandRepository.getHandValue(player2.getId());
            player2DTO = new PublicHandDTO(player2Cards, handValue);
        }
        List<CardDTO> player3Cards = new ArrayList<>();
        PublicHandDTO player3DTO = null;
        if (game.getPlayer3() != null) {
            player3 = playerRepository.findByPlayerName(game.getPlayer3()).orElseThrow(() -> new RuntimeException("Player not found"));
            List<PlayerHand> player3Hand = playerHandRepository.findAllByPlayerId(player3.getId()).orElseThrow();
            for (PlayerHand card : player3Hand) {
                CardDTO cardDTO = new CardDTO(card.getCardValue(), card.getFrontImagePath());
                player3Cards.add(cardDTO);
            }
            int handValue = playerHandRepository.getHandValue(player3.getId());
            player3DTO = new PublicHandDTO(player3Cards, handValue);
        }
        List<CardDTO> player4Cards = new ArrayList<>();
        PublicHandDTO player4DTO = null;
        if (game.getPlayer4() != null) {
            player4 = playerRepository.findByPlayerName(game.getPlayer4()).orElseThrow(() -> new RuntimeException("Player not found"));
            List<PlayerHand> player4Hand = playerHandRepository.findAllByPlayerId(player4.getId()).orElseThrow();
            for (PlayerHand card : player4Hand) {
                CardDTO cardDTO = new CardDTO(card.getCardValue(), card.getFrontImagePath());
                player4Cards.add(cardDTO);
            }
            int handValue = playerHandRepository.getHandValue(player4.getId());
            player4DTO = new PublicHandDTO(player4Cards, handValue);
        }
        List<PublicHandDTO> publicHandDTOList = new ArrayList<>();
        if (player1DTO != null) {
            publicHandDTOList.add(player1DTO);
        } else {
            PublicHandDTO dto = new PublicHandDTO(List.of(), 0);
            publicHandDTOList.add(dto);
        }
        if (player2DTO != null) {
            publicHandDTOList.add(player2DTO);
        } else {
            PublicHandDTO dto = new PublicHandDTO(List.of(), 0);
            publicHandDTOList.add(dto);
        }
        if (player3DTO != null) {
            publicHandDTOList.add(player3DTO);
        } else {
            PublicHandDTO dto = new PublicHandDTO(List.of(), 0);
            publicHandDTOList.add(dto);
        }
        if (player4DTO != null) {
            publicHandDTOList.add(player4DTO);
        } else {
            PublicHandDTO dto = new PublicHandDTO(List.of(), 0);
            publicHandDTOList.add(dto);
        }
        return new PublicHandsDTO(publicHandDTOList, "publicHands.update");
    }

    public String getNextTurnName(Long gameId, String turnPlayerName) {
        Game currentGame = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        String[] players = {currentGame.getPlayer1(), currentGame.getPlayer2(), currentGame.getPlayer3(), currentGame.getPlayer4(), currentGame.getPlayer1(), currentGame.getPlayer2(), currentGame.getPlayer3()};
        for (int i = 0; i < 4; i++) {
            if (players[i] != null && players[i].equals(turnPlayerName)) {
                if (players[i + 1] != null && playerRepository.getPlayerStateByPlayerName(players[i + 1]).equals(PlayerState.WAITING_CARD)) {
                    currentGame.setTurnName(players[i + 1]);
                    gameRepository.save(currentGame);
                    return players[i + 1];
                } else if (players[i + 2] != null && playerRepository.getPlayerStateByPlayerName(players[i + 2]).equals(PlayerState.WAITING_CARD)) {
                    currentGame.setTurnName(players[i + 2]);
                    gameRepository.save(currentGame);
                    return players[i + 2];
                } else if (players[i + 3] != null && playerRepository.getPlayerStateByPlayerName(players[i + 3]).equals(PlayerState.WAITING_CARD)) {
                    currentGame.setTurnName(players[i + 3]);
                    gameRepository.save(currentGame);
                    return players[i + 3];
                } else {
                    currentGame.setTurnName("Dealer");
                    gameRepository.save(currentGame);
                    return "Dealer";
                }
            }
        }
        return null;
    }

    //@Transactional
    public GameMessage handleDealerTurn(Long gameId) {
        Game currentGame = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        Player player1;
        Player player2;
        Player player3;
        Player player4;
        if (currentGame.getPlayer1() == null) {
            player1 = null;
        } else {
            player1 = playerRepository.findByPlayerName(currentGame.getPlayer1()).orElseThrow(() -> new RuntimeException("Player not found"));
        }
        if (currentGame.getPlayer2() == null) {
            player2 = null;
        } else {
            player2 = playerRepository.findByPlayerName(currentGame.getPlayer2()).orElseThrow(() -> new RuntimeException("Player not found"));
        }
        if (currentGame.getPlayer3() == null) {
            player3 = null;
        } else {
            player3 = playerRepository.findByPlayerName(currentGame.getPlayer3()).orElseThrow(() -> new RuntimeException("Player not found"));
        }
        if (currentGame.getPlayer4() == null) {
            player4 = null;
        } else {
            player4 = playerRepository.findByPlayerName(currentGame.getPlayer4()).orElseThrow(() -> new RuntimeException("Player not found"));
        }

        List<Player> playerNamesWithActiveHands = new ArrayList<>();
        if (player1 != null && player1.getPlayerState().equals(PlayerState.ENOUGH)) {
            playerNamesWithActiveHands.add(player1);
            currentGame.setPublicHand1Exists(true);
        }
        if (player2 != null && player2.getPlayerState().equals(PlayerState.ENOUGH)) {
            playerNamesWithActiveHands.add(player2);
            currentGame.setPublicHand2Exists(true);
        }
        if (player3 != null && player3.getPlayerState().equals(PlayerState.ENOUGH)) {
            playerNamesWithActiveHands.add(player3);
            currentGame.setPublicHand3Exists(true);
        }
        if (player4 != null && player4.getPlayerState().equals(PlayerState.ENOUGH)) {
            playerNamesWithActiveHands.add(player4);
            currentGame.setPublicHand4Exists(true);
        }
        //System.out.println("PlayerNamesWithActiveHands: " + playerNamesWithActiveHands.getFirst().getPlayerName());
        if (playerNamesWithActiveHands.isEmpty()) {
            //dealerHandRepository.deleteAllByDealerId(currentGame.getDealerId());
            currentGame.setState(GameState.NEW);
            cardOrder = 1;
            return messageService.gameToMessage(currentGame);
        } else {
            Dealer dealer = dealerRepository.findById(currentGame.getDealerId()).orElseThrow(() -> new RuntimeException("Dealer not found"));
            int dealerHandValue = dealerHandRepository.getHandValue(currentGame.getDealerId());
            while (dealerHandValue < 15) {
                Card newCard = shuffleRepository.findCardByGameIdAndCardOrder(currentGame.getGameId(), cardOrder).orElseThrow(() -> new RuntimeException("Card not found"));
                cardOrder++;
                dealerHandValue = dealerHandValue + newCard.getValue();
                DealerHand dealerHand = new DealerHand();
                dealerHand.setCardValue(newCard.getValue());
                dealerHand.setFrontImagePath(newCard.getFrontImagePath());
                dealerHand.setDealer(dealer);
                dealerHandRepository.save(dealerHand);
                currentGame.setRemainingCards(currentGame.getRemainingCards() - 1);
                if (dealerHandValue >= 15) { // elvileg nem kéne
                    break;  // ha 15 vagy több, azonnal megáll
                }
            }
            String roundResult = "";
            for (Player player : playerNamesWithActiveHands) {
                if (playerHandRepository.getHandValue(player.getId()) > dealerHandValue || dealerHandValue > 22 || (dealerHandValue == 22 && dealerHandRepository.getHandSize(currentGame.getDealerId()) > 2)) {
                    player.setBalance(player.getBalance() + player.getPot());
                    if (player.getPlayerName().equals(currentGame.getPlayer1())) {
                        currentGame.setPlayer1Balance(player.getBalance());
                        roundResult += currentGame.getPlayer1().toUpperCase() + " has won " + player.getPot() / 2 + " $!";
                    } else if (player.getPlayerName().equals(currentGame.getPlayer2())) {
                        currentGame.setPlayer2Balance(player.getBalance());
                        roundResult += currentGame.getPlayer2().toUpperCase() + " has won " + player.getPot() / 2 + " $!";
                    } else if (player.getPlayerName().equals(currentGame.getPlayer3())) {
                        currentGame.setPlayer3Balance(player.getBalance());
                        roundResult += currentGame.getPlayer3().toUpperCase() + " has won " + player.getPot() / 2 + " $!";
                    } else {
                        currentGame.setPlayer4Balance(player.getBalance());
                        roundResult += currentGame.getPlayer4().toUpperCase() + " has won " + player.getPot() / 2 + " $!";
                    }

                } else {
                    dealer.setBalance(dealer.getBalance() + player.getPot());
                    roundResult += "DEALER has won " + player.getPot() / 2 + " $ from " + player.getPlayerName().toUpperCase()+"!";
                }
                player.setPot(0);
                playerRepository.save(player);
                currentGame.setInformation(roundResult);
            }
            dealerRepository.save(dealer);
            currentGame.setDealerBalance(dealer.getBalance());
            currentGame.setState(GameState.NEW);
            gameRepository.save(currentGame);
            cardOrder = 1;
            return messageService.gameToMessage(currentGame);
        }
    }

    public DealerHandDTO getDealerHand(Long gameId) {
        Game currentGame = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        List<DealerHand> dealerCards = dealerHandRepository.findAllByDealerId(currentGame.getDealerId()).orElseThrow(() -> new RuntimeException("Cards not found"));
        List<CardDTO> dealerCardDTOs = new ArrayList<>();
        for (DealerHand dealerHand : dealerCards) {
            CardDTO cardDTO = new CardDTO(dealerHand.getCardValue(), dealerHand.getFrontImagePath());
            dealerCardDTOs.add(cardDTO);
        }
        int dealerHandValue = dealerHandRepository.getHandValue(currentGame.getDealerId());
        return new DealerHandDTO(dealerCardDTOs, dealerHandValue, "dealerHand.update");
    }

    public GameMessage raiseBet(Long gameId, String turnName, int bet) {
        if (bet < 0) {
            throw new IllegalArgumentException("Bet cannot be negative");
        }
        Game currentGame = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        currentGame.setDealerBalance(currentGame.getDealerBalance() - bet);
        Dealer dealer = dealerRepository.findById(currentGame.getDealerId()).orElseThrow(() -> new RuntimeException("Dealer not found"));
        dealer.setBalance(dealer.getBalance() - bet);
        dealerRepository.save(dealer);
        Player player;
        if (turnName.equals(currentGame.getPlayer1())) {
            player = playerRepository.findByPlayerName(currentGame.getPlayer1()).orElseThrow(() -> new RuntimeException("Player not found"));
            if (player.getBalance() < bet) {
                throw new RuntimeException("Player's balance is less than bet");
            }
            player.setBalance(player.getBalance() - bet);
            player.setPot(player.getPot() + bet * 2);
            playerRepository.save(player);
            currentGame.setPlayer1Balance(currentGame.getPlayer1Balance() - bet);
            currentGame.setInformation(player.getPlayerName().toUpperCase() + " placed a " + bet + " $ bet!");
            gameRepository.save(currentGame);
        } else if (turnName.equals(currentGame.getPlayer2())) {
            player = playerRepository.findByPlayerName(currentGame.getPlayer2()).orElseThrow(() -> new RuntimeException("Player not found"));
            if (player.getBalance() < bet) {
                throw new RuntimeException("Player's balance is less than bet");
            }
            player.setBalance(player.getBalance() - bet);
            player.setPot(player.getPot() + bet * 2);
            playerRepository.save(player);
            currentGame.setPlayer2Balance(currentGame.getPlayer2Balance() - bet);
            currentGame.setInformation(player.getPlayerName().toUpperCase() + " placed a " + bet + " $ bet!");
            gameRepository.save(currentGame);
        } else if (turnName.equals(currentGame.getPlayer3())) {
            player = playerRepository.findByPlayerName(currentGame.getPlayer3()).orElseThrow(() -> new RuntimeException("Player not found"));
            if (player.getBalance() < bet) {
                throw new RuntimeException("Player's balance is less than bet");
            }
            player.setBalance(player.getBalance() - bet);
            player.setPot(player.getPot() + bet * 2);
            playerRepository.save(player);
            currentGame.setPlayer3Balance(currentGame.getPlayer3Balance() - bet);
            currentGame.setInformation(player.getPlayerName().toUpperCase() + " placed a " + bet + " $ bet!");
            gameRepository.save(currentGame);
        } else {
            player = playerRepository.findByPlayerName(currentGame.getPlayer4()).orElseThrow(() -> new RuntimeException("Player not found"));
            if (player.getBalance() < bet) {
                throw new RuntimeException("Player's balance is less than bet");
            }
            player.setBalance(player.getBalance() - bet);
            player.setPot(player.getPot() + bet * 2);
            playerRepository.save(player);
            currentGame.setPlayer4Balance(currentGame.getPlayer4Balance() - bet);
            currentGame.setInformation(player.getPlayerName().toUpperCase() + " placed a " + bet + " $ bet!");
            gameRepository.save(currentGame);
        }
        return messageService.gameToMessage(currentGame);
    }

    public PlayerStateDTO getPlayerState(String playerName) {
        PlayerState state = playerRepository.getPlayerStateByPlayerName(playerName);
        return new PlayerStateDTO(state.toString(), "playerState.update");
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
        List<PlayerHand> cards = playerHandRepository.findAllByPlayerId(player.getId()).orElseThrow(() -> new RuntimeException("Cards not found"));
        List<CardDTO> dtos = new ArrayList<>();
        for (PlayerHand card : cards) {
            CardDTO cardDTO = new CardDTO(card.getCardValue(), card.getFrontImagePath());
            dtos.add(cardDTO);
        }
        return new PlayerHandDTO(PlayerState.WAITING_CARD, dtos, 11, "game.throwAce");
    }

    @Transactional
    public GameMessage leaveGame(Long gameId, String playerName) {
        Player leavingPlayer = playerRepository.findByPlayerName(playerName).orElseThrow(()-> new IllegalArgumentException("Player not found"));
        Game currentGame = gameRepository.findById(gameId).orElseThrow(()-> new RuntimeException("Game not found"));
        List<String> players = new ArrayList<>();
        if (currentGame.getPlayer1() != null) {
            players.add(currentGame.getPlayer1());
        }
        if (currentGame.getPlayer2() != null) {
            players.add(currentGame.getPlayer2());
        }
        if (currentGame.getPlayer3() != null) {
            players.add(currentGame.getPlayer3());
        }
        if (currentGame.getPlayer4() != null) {
            players.add(currentGame.getPlayer4());
        }
        if (players.size() == 1) {
            dealerHandRepository.deleteAllByDealerId(currentGame.getDealerId());
            dealerRepository.deleteById((currentGame.getDealerId()));
            gameRepository.delete(currentGame);
            playerHandRepository.deleteAllByPlayerId(leavingPlayer.getId());
            playerRepository.updatePlayerState(leavingPlayer.getId());
            playerRepository.resetCardNumber(playerName);
            playerRepository.save(leavingPlayer);
            shuffleRepository.deleteByGameId(currentGame.getGameId());
            return null;
        } else {
            if (playerName.equals(currentGame.getPlayer1())) {
                playerHandRepository.deleteAllByPlayerId(leavingPlayer.getId());
                leavingPlayer.setPlayerState(PlayerState.WAITING_CARD);
                leavingPlayer.setCardNumber(0);
                if (currentGame.getTurnName().equals(playerName)) {
                    String nextTurnName = getNextTurnName(gameId, playerName);
                    currentGame.setTurnName(nextTurnName);
                }
                currentGame.setPlayer1(null);
                currentGame.setPublicHand1Exists(false);
                if (leavingPlayer.getPot() > 0) {
                    dealerRepository.setDealerBalanceById(leavingPlayer.getPot(), currentGame.getDealerId());
                    leavingPlayer.setPot(0);
                }
                playerRepository.save(leavingPlayer);
            } else if (playerName.equals(currentGame.getPlayer2())) {
                playerHandRepository.deleteAllByPlayerId(leavingPlayer.getId());
                leavingPlayer.setPlayerState(PlayerState.WAITING_CARD);
                leavingPlayer.setCardNumber(0);
                if (currentGame.getTurnName().equals(playerName)) {
                    String nextTurnName = getNextTurnName(gameId, playerName);
                    currentGame.setTurnName(nextTurnName);
                }
                currentGame.setPlayer2(null);
                currentGame.setPublicHand2Exists(false);
                if (leavingPlayer.getPot() > 0) {
                    dealerRepository.setDealerBalanceById(leavingPlayer.getPot(), currentGame.getDealerId());
                    leavingPlayer.setPot(0);
                }
                playerRepository.save(leavingPlayer);
            } else if (playerName.equals(currentGame.getPlayer3())) {
                playerHandRepository.deleteAllByPlayerId(leavingPlayer.getId());
                leavingPlayer.setPlayerState(PlayerState.WAITING_CARD);
                leavingPlayer.setCardNumber(0);
                if (currentGame.getTurnName().equals(playerName)) {
                    String nextTurnName = getNextTurnName(gameId, playerName);
                    currentGame.setTurnName(nextTurnName);
                }
                currentGame.setPlayer3(null);
                currentGame.setPublicHand3Exists(false);
                if (leavingPlayer.getPot() > 0) {
                    dealerRepository.setDealerBalanceById(leavingPlayer.getPot(), currentGame.getDealerId());
                    leavingPlayer.setPot(0);
                }
                playerRepository.save(leavingPlayer);
            } else if (playerName.equals(currentGame.getPlayer4())) {
                playerHandRepository.deleteAllByPlayerId(leavingPlayer.getId());
                leavingPlayer.setPlayerState(PlayerState.WAITING_CARD);
                leavingPlayer.setCardNumber(0);
                if (currentGame.getTurnName().equals(playerName)) {
                    String nextTurnName = getNextTurnName(gameId, playerName);
                    currentGame.setTurnName(nextTurnName);
                }
                currentGame.setPlayer4(null);
                currentGame.setPublicHand4Exists(false);
                if (leavingPlayer.getPot() > 0) {
                    dealerRepository.setDealerBalanceById(leavingPlayer.getPot(), currentGame.getDealerId());
                    leavingPlayer.setPot(0);
                }
                playerRepository.save(leavingPlayer);
            }
        }
        gameRepository.save(currentGame);
        return messageService.gameToMessage(currentGame);
    }

    @Transactional
    public GameMessage throwCards(String playerName, Long gameId) {
        Player playerWithFiveCards = playerRepository.findByPlayerName(playerName).orElseThrow(()-> new IllegalArgumentException("Player not found"));
        playerHandRepository.deleteAllByPlayerId(playerWithFiveCards.getId());
        playerWithFiveCards.setPlayerState(PlayerState.WAITING_CARD);
        playerWithFiveCards.setCardNumber(0);
        playerRepository.save(playerWithFiveCards);
        Game currentGame = gameRepository.findById(gameId).orElseThrow(()-> new RuntimeException("Game not found"));
        currentGame.setInformation(playerName.toUpperCase() + " has thrown 5 card!");
        gameRepository.save(currentGame);
        return messageService.gameToMessage(currentGame);

    }
}
