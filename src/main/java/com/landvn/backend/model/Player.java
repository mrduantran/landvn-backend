package com.landvn.backend.model;

import com.landvn.backend.enums.TokenType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class Player {
    private String id;
    private String username;
    
    private Map<TokenType, Integer> tokens = new HashMap<>();
    private List<Card> purchasedCards = new ArrayList<>();
    private List<Card> reservedCards = new ArrayList<>();
    private List<Noble> nobles = new ArrayList<>();
    
    private int points;
    
    public Player(String id, String username) {
        this.id = id;
        this.username = username;
        // Initialize tokens
        for (TokenType type : TokenType.values()) {
            tokens.put(type, 0);
        }
    }
    
    public int getTotalTokens() {
        return tokens.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    public Map<TokenType, Integer> getBonuses() {
        Map<TokenType, Integer> bonuses = new HashMap<>();
        for (TokenType type : TokenType.values()) {
            if (type == TokenType.GOLD) continue;
            int count = (int) purchasedCards.stream().filter(c -> c.getBonus() == type).count();
            bonuses.put(type, count);
        }
        return bonuses;
    }
}
