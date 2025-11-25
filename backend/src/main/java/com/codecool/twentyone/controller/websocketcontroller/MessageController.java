package com.codecool.twentyone.controller.websocketcontroller;

import com.codecool.twentyone.model.dto.*;
import com.codecool.twentyone.model.entities.*;
import com.codecool.twentyone.repository.*;
import com.codecool.twentyone.service.GameService;
import com.codecool.twentyone.service.MessageService;
import com.codecool.twentyone.service.ShuffleService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

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

    public MessageController(SimpMessagingTemplate messagingTemplate, MessageService messageService, GameService gameService, ShuffleService shuffleService, GameRepository gameRepository, DealerRepository dealerRepository, DealerHandRepository dealerHandRepository, PlayerRepository playerRepository, PlayerHandRepository playerHandRepository) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
        this.gameService = gameService;
        this.shuffleService = shuffleService;
        this.gameRepository = gameRepository;
        this.dealerRepository = dealerRepository;
        this.dealerHandRepository = dealerHandRepository;

        this.playerRepository = playerRepository;
        this.playerHandRepository = playerHandRepository;
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

        headerAccessor.getSessionAttributes().put("gameId", game.getGameId());
        headerAccessor.getSessionAttributes().put("player", message.playerName());



        GameMessage privateMsg = messageService.gameToMessage(game);
        PublicHandsDTO publicHands;
        if (privateMsg.isPublicHand1Exists() || privateMsg.isPublicHand2Exists() || privateMsg.isPublicHand3Exists() || privateMsg.isPublicHand4Exists()) {
            publicHands = gameService.getPublicHandsByNewPlayer(game.getGameId());
            //messagingTemplate.convertAndSend("/topic/game." + game.getGameId(), publicHands);
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/private", publicHands);
        }
        privateMsg.setType("game.joined");
        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/private", privateMsg);

        GameMessage broadcastMsg = messageService.gameToMessage(game);

        broadcastMsg.setType("player.joined");
        messagingTemplate.convertAndSend("/topic/game." + game.getGameId(), broadcastMsg);
    }

    @MessageMapping("/game.leave")
    public void leaveGame(@Payload LeaveMessageDTO request) {
        GameMessage message = gameService.leaveGame(request.gameId(), request.playerName());
        message.setType("player.leaved");

        GameMessage dealerMessage = null;
        DealerHandDTO dealerHand = null;
        PublicHandsDTO publicHands = null;
        if (message.getTurnName().equals("Dealer")) {
            dealerMessage = gameService.handleDealerTurn(request.gameId());
            dealerMessage.setType("game.dealerTurn");
            dealerHand = gameService.getDealerHand(request.gameId());
            publicHands = gameService.getPublicHands(request.gameId());
        }
        message.setType("game.passTurn");
        messagingTemplate.convertAndSend("/topic/game." + request.gameId(), message); // type: game.passTurn
        if (dealerHand != null) {
            messagingTemplate.convertAndSend("/topic/game." + request.gameId(), dealerMessage); // type: game.dealerTurn
            messagingTemplate.convertAndSend("/topic/game." + request.gameId(), dealerHand); // type: dealerHand.update
            messagingTemplate.convertAndSend("/topic/game." + request.gameId(), publicHands); // type: publicHands.update
        }
    }

    @Transactional
    @MessageMapping("/game.firstRound")
    public void sendFirstRound(@Payload GameIdRequest request, Principal principal) {
        ResetHandDTO resetOwnHandDTO = new ResetHandDTO("Reset your hand", "reset.ownHand");
        ResetHandDTO resetPublicHandDTO = new ResetHandDTO("Reset public hands", "reset.publicHands");
        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/private", resetOwnHandDTO);
        messagingTemplate.convertAndSend("/topic/game." + request.gameId(), resetPublicHandDTO);
        shuffleService.addShuffledDeck(request.gameId());
        Game game = gameRepository.findById(request.gameId()).orElseThrow(()-> new RuntimeException("Game not found"));
        game.setRemainingCards(32);
        game.setState(GameState.NEW);
        game.setPublicHand1Exists(false);
        game.setPublicHand2Exists(false);
        game.setPublicHand3Exists(false);
        game.setPublicHand4Exists(false);
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

    @MessageMapping("/game.pullCard")
    public void pullCard(@Payload NewCardRequestDTO request, Principal principal) {
        String playerName = principal.getName();
        if (playerName.equals(request.turnName())) {
            GameMessage message = gameService.pullCard(request.gameId(), playerName);
            GameMessage dealerMessage = null;
            message.setType("game.pullCard");
            PlayerHandDTO hand = gameService.getHand(playerName);
            DealerHandDTO dealerHand = null;
            PublicHandsDTO publicHands = null;
            if (hand.playerState().equals(PlayerState.MUCH) || hand.playerState().equals(PlayerState.FIRE) || hand.playerState().equals(PlayerState.ENOUGH)) {
                String newTurnName = gameService.getNextTurnName(request.gameId(), playerName);
                if (newTurnName.equals("Dealer")) {
                    dealerMessage = gameService.handleDealerTurn(request.gameId());
                    dealerMessage.setType("game.dealerTurn");
                    dealerHand = gameService.getDealerHand(request.gameId());
                    publicHands = gameService.getPublicHands(request.gameId());

                }
                message.setTurnName(newTurnName);
                messagingTemplate.convertAndSend("/topic/game." + request.gameId(), hand);
            }
            messagingTemplate.convertAndSendToUser(playerName, "/queue/private", hand);
            messagingTemplate.convertAndSend("/topic/game." + request.gameId(), message);
            if (dealerHand != null) {
                messagingTemplate.convertAndSend("/topic/game." + request.gameId(), dealerMessage);
                messagingTemplate.convertAndSend("/topic/game." + request.gameId(), dealerHand);
                messagingTemplate.convertAndSend("/topic/game." + request.gameId(), publicHands);
            }
        } else {
            throw new RuntimeException("Invalid turn"); //NotAllowedOperationException
        }
    }

    @MessageMapping("/game.passTurn")
    public void passTurn (@Payload PassTurnRequestDTO request) {
        GameMessage message = gameService.passTurn(request.gameId(), request.turnName());
        GameMessage dealerMessage = null;
        DealerHandDTO dealerHand = null;
        PublicHandsDTO publicHands = null;
        if (message.getTurnName().equals("Dealer")) {
            dealerMessage = gameService.handleDealerTurn(request.gameId());
            dealerMessage.setType("game.dealerTurn");
            dealerHand = gameService.getDealerHand(request.gameId());
            publicHands = gameService.getPublicHands(request.gameId());
        }
        message.setType("game.passTurn");
        messagingTemplate.convertAndSend("/topic/game." + request.gameId(), message); // type: game.passTurn
        if (dealerHand != null) {
            messagingTemplate.convertAndSend("/topic/game." + request.gameId(), dealerMessage); // type: game.dealerTurn
            messagingTemplate.convertAndSend("/topic/game." + request.gameId(), dealerHand); // type: dealerHand.update
            messagingTemplate.convertAndSend("/topic/game." + request.gameId(), publicHands); // type: publicHands.update
        }
    }

    @MessageMapping("/game.raiseBet")
    public void raiseBet(@Payload RaiseBetDTO request) {
        System.out.println("BET REQUEST: " + request);
        GameMessage message = gameService.raiseBet(request.gameId(), request.turnName(), Integer.parseInt(request.bet()));
        message.setType("game.raiseBet");
        PlayerStateDTO dto = gameService.getPlayerState(request.turnName());
        messagingTemplate.convertAndSend("/topic/game." + request.gameId(), message);
        messagingTemplate.convertAndSendToUser(request.turnName(), "/queue/private", dto);
    }
}