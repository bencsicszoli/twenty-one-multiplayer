package com.codecool.twentyone.controller.websocketcontroller;

import com.codecool.twentyone.model.dto.*;
import com.codecool.twentyone.model.dto.websocketdto.*;
import com.codecool.twentyone.model.entities.*;
import com.codecool.twentyone.repository.*;
import com.codecool.twentyone.service.GameService;
import com.codecool.twentyone.service.MessageService;
import com.codecool.twentyone.service.ShuffleService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
public class MessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final GameService gameService;
    private final ShuffleService shuffleService;
    private final GameRepository gameRepository;
    private final DealerRepository dealerRepository;
    private final DealerHandRepository dealerHandRepository;
    private final PlayerRepository playerRepository;
    private final PlayerHandRepository playerHandRepository;
    private final ShuffleRepository shuffleRepository;

    public MessageController(SimpMessagingTemplate messagingTemplate, MessageService messageService, GameService gameService, ShuffleService shuffleService, GameRepository gameRepository, DealerRepository dealerRepository, DealerHandRepository dealerHandRepository, PlayerRepository playerRepository, PlayerHandRepository playerHandRepository, ShuffleRepository shuffleRepository) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
        this.gameService = gameService;
        this.shuffleService = shuffleService;
        this.gameRepository = gameRepository;
        this.dealerRepository = dealerRepository;
        this.dealerHandRepository = dealerHandRepository;
        this.playerRepository = playerRepository;
        this.playerHandRepository = playerHandRepository;
        this.shuffleRepository = shuffleRepository;
    }

    @MessageMapping("/game.join")
    public void joinGame(@Payload JoinMessageDTO message, SimpMessageHeaderAccessor headerAccessor, Principal principal) {
        Game game = messageService.joinGame(message.playerName());
        if (game == null) {
            GameMessage errorMessage = new GameMessage();
            errorMessage.setType("error");
            errorMessage.setContent("We were unable to enter the game.");
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/private", errorMessage);
            return;
        }
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("gameId", game.getGameId());
        headerAccessor.getSessionAttributes().put("player", message.playerName());
        GameMessage joinMessage = messageService.gameToMessage(game);
        joinMessage.setType("game.joined");
        joinMessage.setContent(message.playerName().toUpperCase() + " has joined the game.");
        if (game.getTurnName().equals("Dealer")) {
            DealerHandDTO dealerHandDTO = gameService.getDealerHand(game.getGameId());
            joinMessage.setDealerPublicHand(dealerHandDTO);
        }
        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/private", joinMessage);
        joinMessage.setType("player.joined");
        messagingTemplate.convertAndSend("/topic/game." + game.getGameId(), joinMessage);
    }

@MessageMapping("/game.leave")
public void leaveGame(@Payload LeaveMessageDTO request, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
    String playerName = principal.getName();

    if (!playerName.equals(request.playerName())) {
        throw new RuntimeException("Invalid player name");
    }

    // 👉 explicit logout jelölése
    headerAccessor.getSessionAttributes().put("explicitLogout", true);

    GameMessage message = gameService.leaveGame(request.gameId(), playerName);
    if (message != null) {
        message.setType("player.left");
        messagingTemplate.convertAndSend("/topic/game." + request.gameId(), message);
    }
}

@EventListener
public void onSessionDisconnect(SessionDisconnectEvent event) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

    if (sessionAttributes == null) {
        return;
    }

    // 👉 ha már explicit kilépett, nem csinálunk semmit
    if (Boolean.TRUE.equals(sessionAttributes.get("explicitLogout"))) {
        System.out.println("Session disconnected after explicit logout – skipping leaveGame");
        return;
    }

    Object gameIdObj = sessionAttributes.get("gameId");
    Object playerObj = sessionAttributes.get("player");

    if (gameIdObj == null || playerObj == null) {
        return;
    }

    Long gameId = Long.valueOf(gameIdObj.toString());
    String player = playerObj.toString();

    GameMessage message = gameService.leaveGame(gameId, player);
    if (message != null) {
        message.setType("player.left");
        messagingTemplate.convertAndSend("/topic/game." + gameId, message);
    }
}

    @Transactional
    @MessageMapping("/game.firstRound")
    public void sendFirstRound(@Payload GameIdRequest request, Principal principal) {
        ResetHandDTO resetOwnHandDTO = new ResetHandDTO("Reset your hand", "reset.ownHand");
        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/private", resetOwnHandDTO);
        shuffleService.addShuffledDeck(request.gameId());
        //shuffleService.useFakeDeck(request.gameId());
        Game game = gameRepository.findById(request.gameId()).orElseThrow(()-> new RuntimeException("Game not found"));
        game.setRemainingCards(32);
        game.setState(GameState.NEW);
        game.setPublicHand1Exists(false);
        game.setPublicHand2Exists(false);
        game.setPublicHand3Exists(false);
        game.setPublicHand4Exists(false);
        game.setLastCard(false);
        dealerRepository.setCardNumberById(game.getDealerId());
        dealerHandRepository.deleteAllByDealerId(game.getDealerId());
        List<String> players = new ArrayList<>();
        Player player1 = null;
        Player player2 = null;
        Player player3 = null;
        Player player4;
        if (game.getPlayer1() != null) {
            player1 = playerRepository.findByPlayerName(game.getPlayer1()).orElseThrow(()-> new RuntimeException("Player not found"));
            playerHandRepository.deleteAllByPlayerId(player1.getId());
            player1.setCardNumber(0);
            player1.setPlayerState(PlayerState.WAITING_CARD);
            game.setTurnName(player1.getPlayerName());
            playerRepository.save(player1);
            players.add(game.getPlayer1());
        }
        if (game.getPlayer2() != null) {
            player2 = playerRepository.findByPlayerName(game.getPlayer2()).orElseThrow(()-> new RuntimeException("Player not found"));
            playerHandRepository.deleteAllByPlayerId(player2.getId());
            player2.setCardNumber(0);
            player2.setPlayerState(PlayerState.WAITING_CARD);
            if (player1 == null) {
                game.setTurnName(player2.getPlayerName());
            }
            playerRepository.save(player2);
            players.add(game.getPlayer2());
        }
        if (game.getPlayer3() != null) {
            player3 = playerRepository.findByPlayerName(game.getPlayer3()).orElseThrow(()-> new RuntimeException("Player not found"));
            playerHandRepository.deleteAllByPlayerId(player3.getId());
            player3.setCardNumber(0);
            player3.setPlayerState(PlayerState.WAITING_CARD);
            if (player1 == null && player2 == null) {
                game.setTurnName(player3.getPlayerName());
            }
            players.add(game.getPlayer3());
        }
        if (game.getPlayer4() != null) {
            player4 = playerRepository.findByPlayerName(game.getPlayer4()).orElseThrow(()-> new RuntimeException("Player not found"));
            playerHandRepository.deleteAllByPlayerId(player4.getId());
            player4.setCardNumber(0);
            player4.setPlayerState(PlayerState.WAITING_CARD);
            if (player1 == null && player2 == null && player3 == null) {
                game.setTurnName(player4.getPlayerName());
            }
            players.add(game.getPlayer4());

        }
        game.setState(GameState.IN_PROGRESS);

        for (String player : players) {
            PlayerHandDTO handDTO = gameService.getFirstCard(game.getGameId(), player);
            game.setRemainingCards(game.getRemainingCards() - 1);
            messagingTemplate.convertAndSendToUser(player, "/queue/private", handDTO);
        }
        gameService.giveDealerFirstCard(request.gameId(), game.getDealerId());
        game.setRemainingCards(game.getRemainingCards() - 1);
        game.setInformation("Place some bet or take another card");
        gameRepository.save(game);
        GameMessage message = messageService.gameToMessage(game);
        message.setType("game.firstCard");
        message.setDealerCardNumber(1);
        messagingTemplate.convertAndSend("/topic/game." + game.getGameId(), message);
    }

    @Transactional
    @MessageMapping("/game.pullCard")
    public void pullCard(@Payload NewCardRequestDTO request, Principal principal) {
        String playerName = principal.getName();
        if (playerName.equals(request.turnName())) {
            GameMessage message = gameService.pullCard(request.gameId(), playerName);
            message.setType("game.pullCard");
            PlayerHandDTO hand = gameService.getHand(playerName);
            messagingTemplate.convertAndSendToUser(playerName, "/queue/private", hand);

            if (message.getTurnName().equals("Dealer")) {
                Game currentGame = gameRepository.findById(request.gameId()).orElseThrow(()-> new RuntimeException("Game not found"));
                List<GameMessage> messages = gameService.handleDealerTurn(currentGame);
                for (int i = 0; i < messages.size(); i++) {
                    if (messages.size() == 1) {
                        currentGame.setLastCard(true);
                        gameRepository.save(currentGame);
                        messages.getFirst().setLastCard(true);
                        messages.getFirst().setType("game.pullCard");
                        messagingTemplate.convertAndSend("/topic/game." + request.gameId(), messages.get(i));
                    } else if (i == 0) {
                        messages.getFirst().setType("game.pullCard");
                        messagingTemplate.convertAndSend("/topic/game." + request.gameId(), messages.get(i));
                    } else if (i == messages.size() - 1) {
                        try {
                            Thread.sleep(1000);
                            messages.get(i).setType("game.pullCard");
                            currentGame.setLastCard(true);
                            gameRepository.save(currentGame);
                            messages.getLast().setLastCard(true);
                            messagingTemplate.convertAndSend("/topic/game." + request.gameId(), messages.get(i));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (
                                "Dealer discarded 5 cards!".equals(messages.get(i - 1).getContent()) ||
                                "Dealer announced 'Ohne Ace'".equals(messages.get(i - 1).getContent()) ||
                                "Dealer discarded an Ace after announcing 'Ohne Ace'".equals(messages.get(i - 1).getContent())) {
                            try {
                                Thread.sleep(2500);
                                messages.get(i).setType("game.pullCard");
                                messagingTemplate.convertAndSend("/topic/game." + request.gameId(), messages.get(i));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                Thread.sleep(1000);
                                messages.get(i).setType("game.pullCard");
                                messagingTemplate.convertAndSend("/topic/game." + request.gameId(), messages.get(i));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            } else {
                messagingTemplate.convertAndSend("/topic/game." + request.gameId(), message);
            }

        } else {
            throw new RuntimeException("Invalid turn"); //NotAllowedOperationException
        }
    }

    @MessageMapping("/game.throwAce")
    public void throwAce(@Payload NewCardRequestDTO request, Principal principal) {
        String playerName = principal.getName();
        if (playerName.equals(request.turnName())) {
            GameMessage message = gameService.setContent(request.gameId(), playerName.toUpperCase() +
                    " discarded an Ace after announcing 'Ohne Ace'");
            message.setType("game.throwAce");
            PlayerHandDTO hand = gameService.throwAce(playerName);
            messagingTemplate.convertAndSendToUser(playerName, "/queue/private", hand);
            messagingTemplate.convertAndSend("/topic/game." + request.gameId(), message);
        } else {
            throw new RuntimeException("Invalid turn");
        }
    }

    @Transactional
    @MessageMapping("/game.passTurn")
    public void passTurn (@Payload PassTurnRequestDTO request, Principal principal) {
        String playerName = principal.getName();
        if (playerName.equals(request.turnName())) {
            GameMessage message = gameService.passTurnWhenStand(request.gameId(), request.turnName());
            PlayerStateDTO dto = new PlayerStateDTO(PlayerState.ENOUGH.toString(), "playerState.update");
            message.setType("game.passTurn");
             // type: game.passTurn
            messagingTemplate.convertAndSendToUser(request.turnName(), "/queue/private", dto);

            if (message.getTurnName().equals("Dealer")) {
                Game currentGame = gameRepository.findById(request.gameId()).orElseThrow(()-> new RuntimeException("Game not found"));
                List<GameMessage> messages = gameService.handleDealerTurn(currentGame);
                for (int i = 0; i < messages.size(); i++) {
                    if (messages.size() == 1) {
                        currentGame.setLastCard(true);
                        gameRepository.save(currentGame);
                        messages.getFirst().setLastCard(true);
                        messages.getFirst().setType("game.passTurn");
                        messagingTemplate.convertAndSend("/topic/game." + request.gameId(), messages.get(i));
                    } else if (i == 0) {
                        messages.getFirst().setType("game.passTurn");
                        messagingTemplate.convertAndSend("/topic/game." + request.gameId(), messages.get(i));
                    } else if (i == messages.size() - 1) {
                        try {
                            Thread.sleep(1000);
                            messages.get(i).setType("game.passTurn");
                            currentGame.setLastCard(true);
                            messages.getLast().setLastCard(true);
                            messagingTemplate.convertAndSend("/topic/game." + request.gameId(), messages.get(i));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (
                                "Dealer discarded 5 cards!".equals(messages.get(i - 1).getContent()) ||
                                "Dealer announced 'Ohne Ace'".equals(messages.get(i - 1).getContent()) ||
                                "Dealer discarded an Ace after announcing 'Ohne Ace'".equals(messages.get(i - 1).getContent())) {
                            try {
                                Thread.sleep(2500);
                                messages.get(i).setType("game.passTurn");
                                messagingTemplate.convertAndSend("/topic/game." + request.gameId(), messages.get(i));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                Thread.sleep(1000);
                                messages.get(i).setType("game.passTurn");
                                messagingTemplate.convertAndSend("/topic/game." + request.gameId(), messages.get(i));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } else {
                messagingTemplate.convertAndSend("/topic/game." + request.gameId(), message);
            }
        } else {
            throw new RuntimeException("Invalid turn");
        }
    }

    @MessageMapping("/game.raiseBet")
    public void raiseBet(@Payload RaiseBetDTO request, Principal principal) {
        String playerName = principal.getName();
        if (playerName.equals(request.turnName())) {
            GameMessage message = gameService.raiseBet(request.gameId(), request.turnName(), Integer.parseInt(request.bet()));
            message.setType("game.raiseBet");
            PlayerStateDTO dto = gameService.getPlayerState(request.turnName());
            messagingTemplate.convertAndSendToUser(request.turnName(), "/queue/private", dto);
            messagingTemplate.convertAndSend("/topic/game." + request.gameId(), message);
        } else {
            throw new RuntimeException("Invalid turn");
        }
    }

    @MessageMapping("/game.throwCards")
    public void throwCards(@Payload NewCardRequestDTO request, Principal principal) {
        String playerName = principal.getName();
        if (playerName.equals(request.turnName())) {
            GameMessage message = gameService.throwCards(playerName, request.gameId());
            message.setType("game.throwCards");
            PlayerHandDTO dto = new PlayerHandDTO(PlayerState.WAITING_CARD, List.of(), 0, "hand.update");
            messagingTemplate.convertAndSend("/topic/game." + request.gameId(), message);
            messagingTemplate.convertAndSendToUser(playerName, "/queue/private", dto);
        } else {
            throw new RuntimeException("Invalid turn");
        }
    }

    @MessageMapping("/game.ohneAce")
    public void setOhneAceState(@Payload NewCardRequestDTO request, Principal principal) {
        String playerName = principal.getName();
        if (playerName.equals(request.turnName())) {
            PlayerStateDTO dto = gameService.setPlayerStateToOhneAce(playerName);
            GameMessage message = gameService.setContent(request.gameId(), playerName.toUpperCase() + " announced 'Ohne Ace'");
            message.setType("game.newContent");
            messagingTemplate.convertAndSend("/topic/game." + request.gameId(), message);
            messagingTemplate.convertAndSendToUser(playerName, "/queue/private", dto);
        } else {
            throw new RuntimeException("Invalid turn");
        }
    }
}