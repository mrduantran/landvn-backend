package com.landvn.backend.service;

import com.landvn.backend.model.GameState;
import com.landvn.backend.model.Player;
import com.landvn.backend.model.RoomEntity;
import com.landvn.backend.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Service
public class RoomService {
    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public GameState createRoom(String roomName) {
        String roomId = UUID.randomUUID().toString().substring(0, 8);
        return createRoom(roomName, roomId);
    }

    private GameState createRoom(String roomName, String roomId) {
        GameState game = new GameState(roomId);
        
        RoomEntity room = RoomEntity.builder()
                .id(roomId)
                .name(roomName)
                .status("WAITING")
                .gameState(game)
                .build();
                
        roomRepository.save(room);
        return game;
    }

    public GameState getRoom(String roomId) {
        return roomRepository.findById(roomId).map(RoomEntity::getGameState).orElse(null);
    }

    public void saveRoom(GameState state) {
        RoomEntity room = roomRepository.findById(state.getRoomId()).orElse(null);
        if (room != null) {
            room.setGameState(state);
            room.setStatus(state.getStatus());
            room.setWinnerId(state.getWinnerId());
            roomRepository.save(room);
        }
    }

    public List<GameState> listRooms() {
        List<GameState> games = new ArrayList<>();
        roomRepository.findAll().forEach(r -> {
            if (!r.isDeleted() && r.getGameState() != null) games.add(r.getGameState());
        });
        return games;
    }

    public GameState joinRoom(String roomId, String username) throws Exception {
        RoomEntity room = roomRepository.findById(roomId).orElseThrow(() -> new Exception("Room not found"));
        GameState state = room.getGameState();
        
        boolean alreadyJoined = state.getPlayers().stream().anyMatch(p -> p.getId().equals(username));
        if (!alreadyJoined) {
            if (state.getPlayers().size() >= 4) {
                throw new Exception("Room is full");
            }
            if (!"WAITING".equals(state.getStatus())) {
                throw new Exception("Game already started");
            }
            
            Player newPlayer = new Player(username, username);
            state.getPlayers().add(newPlayer);
            
            room.setGameState(state);
            room.setStatus(state.getStatus());
            roomRepository.save(room);
        }
        return state;
    }

    public GameState startGame(String roomId) throws Exception {
        RoomEntity room = roomRepository.findById(roomId).orElseThrow(() -> new Exception("Room not found"));
        GameState state = room.getGameState();
        
        if (!"WAITING".equals(state.getStatus())) {
            throw new Exception("Game already started");
        }
        
        state.startGame();
        room.setGameState(state);
        room.setStatus(state.getStatus());
        roomRepository.save(room);
        return state;
    }

    public GameState leaveRoom(String roomId, String username) throws Exception {
        RoomEntity room = roomRepository.findById(roomId).orElseThrow(() -> new Exception("Room not found"));
        GameState state = room.getGameState();
        
        boolean removed = state.getPlayers().removeIf(p -> p.getId().equals(username));
        if (removed) {
            if (state.getPlayers().isEmpty()) {
                room.setDeleted(true);
                roomRepository.save(room);
            } else {
                room.setGameState(state);
                roomRepository.save(room);
            }
        }
        return state;
    }

    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 60000)
    public void cleanupStaleRooms() {
        java.time.LocalDateTime fiveMinsAgo = java.time.LocalDateTime.now().minusMinutes(5);
        roomRepository.findAll().forEach(room -> {
            if (!room.isDeleted() && "WAITING".equals(room.getStatus())) {
                if (room.getCreatedAt() == null || room.getCreatedAt().isBefore(fiveMinsAgo)) {
                    room.setDeleted(true);
                    roomRepository.save(room);
                }
            }
        });
    }
}
