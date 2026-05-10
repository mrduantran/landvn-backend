package com.landvn.backend.controller;

import com.landvn.backend.enums.TokenType;
import com.landvn.backend.model.GameState;
import com.landvn.backend.service.GameService;
import lombok.Data;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class GameController {

    private final GameService gameService;
    private final com.landvn.backend.service.RoomService roomService;

    public GameController(GameService gameService, com.landvn.backend.service.RoomService roomService) {
        this.gameService = gameService;
        this.roomService = roomService;
    }

    @GetMapping("/api/game/{roomId}")
    public GameState getGameState(@PathVariable String roomId) {
        GameState state = roomService.getRoom(roomId);
        if (state == null) throw new RuntimeException("Game not found");
        return state;
    }

    @MessageMapping("/game/{roomId}/take-tokens")
    @SendTo("/topic/game/{roomId}")
    public GameState handleTakeTokens(@DestinationVariable String roomId, @Payload TakeTokensRequest request) {
        GameState state = roomService.getRoom(roomId);
        if (state == null) {
            throw new RuntimeException("Game not found");
        }
        try {
            gameService.takeTokens(state, request.getPlayerId(), request.getTokens(), request.getReturnedTokens());
            roomService.saveRoom(state);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return state;
    }
    
    @MessageMapping("/game/{roomId}/buy-card")
    @SendTo("/topic/game/{roomId}")
    public GameState handleBuyCard(@DestinationVariable String roomId, @Payload BuyCardRequest request) {
        GameState state = roomService.getRoom(roomId);
        if (state == null) {
            throw new RuntimeException("Game not found");
        }
        try {
            gameService.buyCard(state, request.getPlayerId(), request.getCardId());
            roomService.saveRoom(state);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return state;
    }
}

@Data
class TakeTokensRequest {
    private String playerId;
    private List<TokenType> tokens;
    private List<TokenType> returnedTokens;
}

@Data
class BuyCardRequest {
    private String playerId;
    private String cardId;
}
