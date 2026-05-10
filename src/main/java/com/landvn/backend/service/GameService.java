package com.landvn.backend.service;

import com.landvn.backend.enums.TokenType;
import com.landvn.backend.model.Card;
import com.landvn.backend.model.GameState;
import com.landvn.backend.model.Player;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GameService {
    
    public void takeTokens(GameState state, String playerId, List<TokenType> requestedTokens, List<TokenType> returnedTokens) throws Exception {
        Player player = getPlayer(state, playerId);
        checkPlayerTurn(state, playerId);
        
        if (requestedTokens.contains(TokenType.GOLD)) {
            throw new Exception("Cannot take gold directly");
        }

        if (requestedTokens.size() == 3) {
            long distinctCount = requestedTokens.stream().distinct().count();
            if (distinctCount != 3) {
                throw new Exception("Must choose 3 different tokens");
            }
            for (TokenType token : requestedTokens) {
                if (state.getBankTokens().getOrDefault(token, 0) < 1) {
                    throw new Exception("Not enough tokens in bank for " + token);
                }
            }
        } else if (requestedTokens.size() == 2) {
            TokenType token = requestedTokens.get(0);
            if (requestedTokens.get(1) != token) {
                throw new Exception("Must be 2 of the same token");
            }
            if (state.getBankTokens().getOrDefault(token, 0) < 4) {
                throw new Exception("Bank must have at least 4 tokens to take 2 of the same color");
            }
        } else {
            throw new Exception("Invalid token amount requested");
        }

        Map<TokenType, Integer> tempPlayerTokens = new java.util.HashMap<>(player.getTokens());
        for (TokenType token : requestedTokens) {
            tempPlayerTokens.put(token, tempPlayerTokens.getOrDefault(token, 0) + 1);
        }

        if (returnedTokens != null) {
            for (TokenType token : returnedTokens) {
                int current = tempPlayerTokens.getOrDefault(token, 0);
                if (current < 1) {
                    throw new Exception("Cannot return token you don't have: " + token);
                }
                tempPlayerTokens.put(token, current - 1);
            }
        }

        int totalTokensAfter = tempPlayerTokens.values().stream().mapToInt(Integer::intValue).sum();
        if (totalTokensAfter > 10) {
            throw new Exception("Player cannot hold more than 10 tokens. Please return some tokens.");
        }

        for (TokenType token : requestedTokens) {
            state.getBankTokens().put(token, state.getBankTokens().get(token) - 1);
        }
        if (returnedTokens != null) {
            for (TokenType token : returnedTokens) {
                state.getBankTokens().put(token, state.getBankTokens().getOrDefault(token, 0) + 1);
            }
        }
        player.setTokens(tempPlayerTokens);

        nextTurn(state);
    }

    public void buyCard(GameState state, String playerId, String cardId) throws Exception {
        Player player = getPlayer(state, playerId);
        checkPlayerTurn(state, playerId);

        Card cardToBuy = findCardOnTableOrReserved(state, player, cardId);
        
        Map<TokenType, Integer> cost = cardToBuy.getCost();
        Map<TokenType, Integer> playerBonuses = player.getBonuses();
        Map<TokenType, Integer> playerTokens = player.getTokens();
        
        int missingTokens = 0;
        
        for (Map.Entry<TokenType, Integer> entry : cost.entrySet()) {
            TokenType requiredType = entry.getKey();
            int requiredAmount = entry.getValue();
            
            int actualCost = Math.max(0, requiredAmount - playerBonuses.getOrDefault(requiredType, 0));
            int availableTokens = playerTokens.getOrDefault(requiredType, 0);
            
            if (availableTokens < actualCost) {
                missingTokens += (actualCost - availableTokens);
            }
        }
        
        int playerGold = playerTokens.getOrDefault(TokenType.GOLD, 0);
        if (missingTokens > playerGold) {
            throw new Exception("Not enough resources to buy this card");
        }
        
        for (Map.Entry<TokenType, Integer> entry : cost.entrySet()) {
            TokenType requiredType = entry.getKey();
            int requiredAmount = entry.getValue();
            
            int actualCost = Math.max(0, requiredAmount - playerBonuses.getOrDefault(requiredType, 0));
            int availableTokens = playerTokens.getOrDefault(requiredType, 0);
            
            int tokensToPay = Math.min(actualCost, availableTokens);
            
            playerTokens.put(requiredType, availableTokens - tokensToPay);
            state.getBankTokens().put(requiredType, state.getBankTokens().getOrDefault(requiredType, 0) + tokensToPay);
        }
        
        if (missingTokens > 0) {
            playerTokens.put(TokenType.GOLD, playerGold - missingTokens);
            state.getBankTokens().put(TokenType.GOLD, state.getBankTokens().getOrDefault(TokenType.GOLD, 0) + missingTokens);
        }
        
        player.getPurchasedCards().add(cardToBuy);
        player.setPoints(player.getPoints() + cardToBuy.getPoints());
        
        removeCardFromTableOrReserved(state, player, cardToBuy);
        
        nextTurn(state);
    }
    
    private Player getPlayer(GameState state, String playerId) throws Exception {
        return state.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new Exception("Player not found"));
    }
    
    private void checkPlayerTurn(GameState state, String playerId) throws Exception {
        Player currentPlayer = state.getPlayers().get(state.getCurrentPlayerIndex());
        if (!currentPlayer.getId().equals(playerId)) {
            throw new Exception("Not your turn");
        }
    }
    
    private void nextTurn(GameState state) {
        state.setCurrentPlayerIndex((state.getCurrentPlayerIndex() + 1) % state.getPlayers().size());
    }
    
    private Card findCardOnTableOrReserved(GameState state, Player player, String cardId) throws Exception {
        for (Card c : state.getTableLevel1()) if (c.getId().equals(cardId)) return c;
        for (Card c : state.getTableLevel2()) if (c.getId().equals(cardId)) return c;
        for (Card c : state.getTableLevel3()) if (c.getId().equals(cardId)) return c;
        for (Card c : player.getReservedCards()) if (c.getId().equals(cardId)) return c;
        throw new Exception("Card not found");
    }

    private void removeCardFromTableOrReserved(GameState state, Player player, Card card) {
        if (state.getTableLevel1().remove(card)) { replenishTable(state, 1); return; }
        if (state.getTableLevel2().remove(card)) { replenishTable(state, 2); return; }
        if (state.getTableLevel3().remove(card)) { replenishTable(state, 3); return; }
        player.getReservedCards().remove(card);
    }

    private void replenishTable(GameState state, int level) {
        if (level == 1 && !state.getDeckLevel1().isEmpty()) {
            state.getTableLevel1().add(state.getDeckLevel1().remove(0));
        } else if (level == 2 && !state.getDeckLevel2().isEmpty()) {
            state.getTableLevel2().add(state.getDeckLevel2().remove(0));
        } else if (level == 3 && !state.getDeckLevel3().isEmpty()) {
            state.getTableLevel3().add(state.getDeckLevel3().remove(0));
        }
    }
}
