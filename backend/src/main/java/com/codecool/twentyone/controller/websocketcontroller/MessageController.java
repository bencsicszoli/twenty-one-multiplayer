package com.codecool.twentyone.controller.websocketcontroller;

import com.codecool.twentyone.model.dto.GameMessage;
import com.codecool.twentyone.model.dto.HandDTO;
import com.codecool.twentyone.model.dto.JoinMessageDTO;
import com.codecool.twentyone.model.entities.Game;
import com.codecool.twentyone.service.GameService;
import com.codecool.twentyone.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import java.util.Map;

@Controller
public class MessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final GameService gameService;

    public MessageController(SimpMessagingTemplate messagingTemplate, MessageService messageService, GameService gameService) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
        this.gameService = gameService;
    }

    @MessageMapping("/game.join")
    @SendTo("/topic/game.state")
    public Object joinGame(@Payload JoinMessageDTO message, SimpMessageHeaderAccessor headerAccessor) {
        Game game = messageService.joinGame(message.playerName());
        if (game == null) {
            GameMessage errorMessage = new GameMessage();
            errorMessage.setType("error");
            errorMessage.setContent("We were unable to enter the game. Perhaps the game is already full or there was an internal error.");
            return errorMessage;
        }
        headerAccessor.getSessionAttributes().put("gameId", game.getGameId());
        headerAccessor.getSessionAttributes().put("player", message.playerName());

        GameMessage gameMessage = messageService.gameToMessage(game);
        gameMessage.setType("game.joined");
        return gameMessage;
    }

    @MessageMapping("/game.firstCard")
    public void sendFirstCard(@Payload Map<String, Object> payload, Principal principal) {
        Number idNumber = (Number) payload.get("gameId");
        Long gameId = idNumber.longValue();
        String username = principal.getName();
        HandDTO firstCard = gameService.getFirstCard(gameId);
        GameMessage message = gameService.pullCard(gameId, username);
        message.setType("game.pullCard");
        messagingTemplate.convertAndSendToUser(username, "/queue/private", firstCard);
        messagingTemplate.convertAndSend("/topic/game." + gameId, message);
    }
}