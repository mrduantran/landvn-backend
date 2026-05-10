package com.landvn.backend.controller;

import com.landvn.backend.model.GameState;
import com.landvn.backend.service.RoomService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    
    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    public RoomController(RoomService roomService, SimpMessagingTemplate messagingTemplate) {
        this.roomService = roomService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public List<GameState> listRooms() {
        return roomService.listRooms();
    }

    @PostMapping
    public GameState createRoom(@RequestBody CreateRoomRequest request) {
        return roomService.createRoom(request.getRoomName());
    }

    @PostMapping("/{roomId}/join")
    public GameState joinRoom(@PathVariable String roomId, Authentication authentication) throws Exception {
        GameState state = roomService.joinRoom(roomId, authentication.getName());
        messagingTemplate.convertAndSend("/topic/game/" + roomId, state);
        return state;
    }

    @PostMapping("/{roomId}/leave")
    public GameState leaveRoom(@PathVariable String roomId, Authentication authentication) throws Exception {
        GameState state = roomService.leaveRoom(roomId, authentication.getName());
        messagingTemplate.convertAndSend("/topic/game/" + roomId, state);
        return state;
    }

    @PostMapping("/{roomId}/start")
    public GameState startGame(@PathVariable String roomId) throws Exception {
        GameState state = roomService.startGame(roomId);
        messagingTemplate.convertAndSend("/topic/game/" + roomId, state);
        return state;
    }
}

@Data
class CreateRoomRequest {
    private String roomName;
}
