package com.landvn.backend.model;

import com.landvn.backend.enums.TokenType;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameState {
    private String roomId;
    private Map<TokenType, Integer> bankTokens = new HashMap<>();
    
    private List<Card> deckLevel1 = new ArrayList<>();
    private List<Card> deckLevel2 = new ArrayList<>();
    private List<Card> deckLevel3 = new ArrayList<>();
    
    private List<Card> tableLevel1 = new ArrayList<>();
    private List<Card> tableLevel2 = new ArrayList<>();
    private List<Card> tableLevel3 = new ArrayList<>();
    
    private List<Noble> tableNobles = new ArrayList<>();
    
    private List<Player> players = new ArrayList<>();
    private int currentPlayerIndex = 0;
    
    private String status; // WAITING, PLAYING, FINISHED
    private String winnerId;
    
    public GameState(String roomId) {
        this.roomId = roomId;
        this.status = "WAITING";
    }

    public void startGame() {
        if (this.players.size() < 2) {
            throw new RuntimeException("Not enough players to start game");
        }
        this.status = "PLAYING";
        
        // Init Tokens based on player count (2 players = 4, 3 players = 5, 4 players = 7)
        int tokenCount = this.players.size() == 2 ? 4 : (this.players.size() == 3 ? 5 : 7);
        for (TokenType type : TokenType.values()) {
            if (type == TokenType.GOLD) this.bankTokens.put(type, 5); // Gold is always 5
            else this.bankTokens.put(type, tokenCount);
        }

        // Init Decks
        this.deckLevel1 = com.landvn.backend.util.DeckInitializer.generateLevel1Deck();
        this.deckLevel2 = com.landvn.backend.util.DeckInitializer.generateLevel2Deck();
        this.deckLevel3 = com.landvn.backend.util.DeckInitializer.generateLevel3Deck();

        // Deal 4 cards to table per level
        for (int i = 0; i < 4; i++) {
            this.tableLevel1.add(this.deckLevel1.remove(0));
            this.tableLevel2.add(this.deckLevel2.remove(0));
            this.tableLevel3.add(this.deckLevel3.remove(0));
        }

        // Init Nobles (Player count + 2 as requested)
        this.tableNobles = com.landvn.backend.util.DeckInitializer.generateNobles(this.players.size() + 2);
        
        this.currentPlayerIndex = 0;
    }
}
