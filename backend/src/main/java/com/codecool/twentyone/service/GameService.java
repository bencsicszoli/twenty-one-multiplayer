package com.codecool.twentyone.service;

import com.codecool.twentyone.model.dto.*;
import com.codecool.twentyone.model.entities.*;
import com.codecool.twentyone.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final PlayerHandRepository playerHandRepository;
    private final DealerHandRepository dealerHandRepository;
    private final DealerRepository dealerRepository;
    private final ShuffleRepository shuffleRepository;
    private static int cardOrder = 1;
    private static int handleDealerTurnCounter = 0;
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
        System.out.println("Card order in getFirsCard method: " + cardOrder);
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
        System.out.println("Card order in giveDealerFirstCard method: " + cardOrder);
        dealer.setCardNumber(1);
        dealerRepository.save(dealer);
    }

    public GameMessage pullCard(Long gameId, String playerName) {
        Player currentPlayer = playerRepository.findByPlayerName(playerName).orElseThrow(() -> new RuntimeException("Player not found"));
        currentPlayer.setCardNumber(currentPlayer.getCardNumber() + 1);
        playerRepository.save(currentPlayer);
        Card card = shuffleRepository.findCardByGameIdAndCardOrder(gameId, cardOrder).orElseThrow(() -> new RuntimeException("Card not found"));
        cardOrder++;
        System.out.println("Card order in pullCard method: " + cardOrder);
        PlayerHand handCard = new PlayerHand();
        handCard.setCardValue(card.getValue());
        handCard.setFrontImagePath(card.getFrontImagePath());
        handCard.setPlayer(currentPlayer);
        playerHandRepository.save(handCard);
        Game currentGame = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        currentGame.setRemainingCards(currentGame.getRemainingCards() - 1);

        Game updatedGame = gameRepository.save(currentGame);
        return messageService.gameToMessage(updatedGame);
    }
    public GameMessage passTurn(Long gameId, String turnPlayerName) {
        Player currentPlayer = playerRepository.findByPlayerName(turnPlayerName).orElseThrow(() -> new RuntimeException("Player not found"));
        currentPlayer.setPlayerState(PlayerState.ENOUGH);
        playerRepository.save(currentPlayer);
        Game currentGame = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        String[] players = {currentGame.getPlayer1(), currentGame.getPlayer2(), currentGame.getPlayer3(), currentGame.getPlayer1(), currentGame.getPlayer2()};
        for (int i = 0; i < 3; i++) {
            if (players[i] != null && players[i].equals(turnPlayerName)) {
                if (players[i + 1] != null && playerRepository.getPlayerStateByPlayerName(players[i + 1]).equals(PlayerState.WAITING_CARD)) {
                    currentGame.setTurnName(players[i + 1]);
                    gameRepository.save(currentGame);
                    return messageService.gameToMessage(currentGame);
                } else if (players[i + 2] != null && playerRepository.getPlayerStateByPlayerName(players[i + 2]).equals(PlayerState.WAITING_CARD)) {
                    currentGame.setTurnName(players[i + 2]);
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

    public PlayerHandDTO getHand(Long gameId, String playerName) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        Dealer dealer = dealerRepository.findById(game.getDealerId()).orElseThrow(() -> new RuntimeException("Dealer not found"));
        Player player = playerRepository.findByPlayerName(playerName).orElseThrow(() -> new RuntimeException("Player not found"));
        int handValue = playerHandRepository.getHandValue(player.getId());
        int cardsNumber = playerHandRepository.getHandSize(player.getId());
        if (handValue > 14 && handValue < 20) {
            player.setPlayerState(PlayerState.COULD_STOP);
        } else if (handValue > 19 && handValue < 22) {
            player.setPlayerState(PlayerState.ENOUGH);
        } else if (handValue == 22 && cardsNumber == 2) {
            player.setPlayerState(PlayerState.FIRE);
            player.setBalance(player.getBalance() + player.getPot());
            player.setPot(0);
            if (game.getPlayer1().equals(playerName)) {
                game.setPlayer1Balance(player.getBalance());
            } else if (game.getPlayer2().equals(playerName)) {
                game.setPlayer2Balance(player.getBalance());
            } else if (game.getPlayer3().equals(playerName)) {
                game.setPlayer3Balance(player.getBalance());
            }
        } else if (handValue >= 22) {
            player.setPlayerState(PlayerState.MUCH);
            dealer.setBalance(dealer.getBalance() + player.getPot());
            game.setDealerBalance(dealer.getBalance());
            player.setPot(0);
        }
        gameRepository.save(game);
        playerRepository.save(player);
        List<PlayerHand> ownCards = playerHandRepository.findAllByPlayerId(player.getId()).orElseThrow(() -> new RuntimeException("Cards not found"));
        List<CardDTO> cardDTOList = new ArrayList<>();
        for (PlayerHand card : ownCards) {
            cardDTOList.add(new CardDTO(card.getCardValue(), card.getFrontImagePath()));
        }
        return new PlayerHandDTO(player.getPlayerState(), cardDTOList, handValue, "hand.update");
    }

    public PublicHandsDTO getPublicHands(Long gameId) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        Player player1;
        Player player2;
        Player player3;
        List<CardDTO> player1Cards = new ArrayList<>();
        PublicHandDTO player1DTO = null;
        if (game.getPlayer1() != null) {
            player1 = playerRepository.findByPlayerName(game.getPlayer1()).orElseThrow(() -> new RuntimeException("Player not found"));
            List<PlayerHand> player1Hand = playerHandRepository.findAllByPlayerId(player1.getId()).orElseThrow();
            for (PlayerHand card : player1Hand) {
                CardDTO cardDTO = new CardDTO(card.getCardValue(), card.getFrontImagePath());
                player1Cards.add(cardDTO);
            }
            player1DTO = new PublicHandDTO(player1Cards);
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
            player2DTO = new PublicHandDTO(player2Cards);
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
            player3DTO = new PublicHandDTO(player3Cards);
        }
        List<PublicHandDTO> publicHandDTOList = new ArrayList<>();
        if (player1DTO != null) {
            publicHandDTOList.add(player1DTO);
        } else {
            PublicHandDTO dto = new PublicHandDTO(List.of());
            publicHandDTOList.add(dto);
        }
        if (player2DTO != null) {
            publicHandDTOList.add(player2DTO);
        } else {
            PublicHandDTO dto = new PublicHandDTO(List.of());
            publicHandDTOList.add(dto);
        }
        if (player3DTO != null) {
            publicHandDTOList.add(player3DTO);
        } else {
            PublicHandDTO dto = new PublicHandDTO(List.of());
            publicHandDTOList.add(dto);
        }
        return new PublicHandsDTO(publicHandDTOList, "publicHands.update");
    }

    public String getNextTurnName(Long gameId, String turnPlayerName) {
        Game currentGame = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        String[] players = {currentGame.getPlayer1(), currentGame.getPlayer2(), currentGame.getPlayer3(), currentGame.getPlayer1(), currentGame.getPlayer2()};
        for (int i = 0; i < 3; i++) {
            if (players[i] != null && players[i].equals(turnPlayerName)) {
                if (players[i + 1] != null && playerRepository.getPlayerStateByPlayerName(players[i + 1]).equals(PlayerState.WAITING_CARD)) {
                    currentGame.setTurnName(players[i + 1]);
                    gameRepository.save(currentGame);
                    return players[i + 1];
                } else if (players[i + 2] != null && playerRepository.getPlayerStateByPlayerName(players[i + 2]).equals(PlayerState.WAITING_CARD)) {
                    currentGame.setTurnName(players[i + 2]);
                    gameRepository.save(currentGame);
                    return players[i + 2];
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
        handleDealerTurnCounter++;
        System.out.println("Counter: " + handleDealerTurnCounter);
        Game currentGame = gameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        Player player1;
        Player player2;
        Player player3;
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

        List<Player> playerNamesWithActiveHands = new ArrayList<>();
        if (player1 != null && player1.getPlayerState().equals(PlayerState.ENOUGH)) {
            playerNamesWithActiveHands.add(player1);
        }
        if (player2 != null && player2.getPlayerState().equals(PlayerState.ENOUGH)) {
            playerNamesWithActiveHands.add(player2);
        }

        if (player3 != null && player3.getPlayerState().equals(PlayerState.ENOUGH)) {
            playerNamesWithActiveHands.add(player3);
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
            System.out.println("Dealer Hand Value before while loop: " + dealerHandValue);
            while (dealerHandValue < 15) {
                Card newCard = shuffleRepository.findCardByGameIdAndCardOrder(currentGame.getGameId(), cardOrder).orElseThrow(() -> new RuntimeException("Card not found"));
                cardOrder++;
                System.out.println("Card Order in while loop: " + cardOrder);
                System.out.println("New card value: " + newCard.getValue());
                int nextValue = dealerHandValue + newCard.getValue();
                System.out.println("Next Card Value: " + nextValue);
                dealerHandValue = nextValue;
                System.out.println("Dealer Hand Value: " + dealerHandValue);
                DealerHand dealerHand = new DealerHand();
                dealerHand.setCardValue(newCard.getValue());
                dealerHand.setFrontImagePath(newCard.getFrontImagePath());
                dealerHand.setDealer(dealer);
                dealerHandRepository.save(dealerHand);
                currentGame.setRemainingCards(currentGame.getRemainingCards() - 1);
                if (dealerHandValue >= 15) {
                    break;  // ha 15 vagy több, azonnal megáll
                }
            }

            for (Player player : playerNamesWithActiveHands) {
                if (playerHandRepository.getHandValue(player.getId()) > dealerHandValue || dealerHandValue > 22 || (dealerHandValue == 22 && dealerHandRepository.getHandSize(currentGame.getDealerId()) > 2)) {
                    player.setBalance(player.getBalance() + player.getPot());
                    if (player.getPlayerName().equals(currentGame.getPlayer1())) {
                        currentGame.setPlayer1Balance(player.getBalance());
                    } else if (player.getPlayerName().equals(currentGame.getPlayer2())) {
                        currentGame.setPlayer2Balance(player.getBalance());
                    } else {
                        currentGame.setPlayer3Balance(player.getBalance());
                    }
                } else {
                    dealer.setBalance(dealer.getBalance() + player.getPot());
                }
            }
            dealerRepository.save(dealer);
            currentGame.setDealerBalance(dealer.getBalance());
            currentGame.setState(GameState.NEW);
            gameRepository.save(currentGame);
            cardOrder = 1;
            return messageService.gameToMessage(currentGame);
        }
    }

    public DealerHandDTO getDealerHand(Long dealerId) {
        List<DealerHand> dealerCards = dealerHandRepository.findAllByDealerId(dealerId).orElseThrow(() -> new RuntimeException("Cards not found"));
        List<CardDTO> dealerCardDTOs = new ArrayList<>();
        int dealerHandValue = dealerHandRepository.getHandValue(dealerId);
        for (DealerHand dealerHand : dealerCards) {
            CardDTO cardDTO = new CardDTO(dealerHand.getCardValue(), dealerHand.getFrontImagePath());
            dealerCardDTOs.add(cardDTO);
        }
        return new DealerHandDTO(dealerCardDTOs, dealerHandValue, "dealerHand.update");
    }

    public Map<String, String> cleanGameForTestingPurpose() {
        gameRepository.deleteAll();
        playerHandRepository.deleteAll();
        playerRepository.updatePlayerState(1L);
        playerRepository.updatePlayerState(2L);
        playerRepository.updateCardNumber(0);
        Map<String, String> map = new HashMap<>();
        map.put("message", "Game has been cleaned!");
        return map;
    }
}
