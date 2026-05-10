package com.landvn.backend.model;

import com.landvn.backend.enums.TokenType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class Card {
    private String id;
    private int level; // 1, 2, 3
    private int points; // Prestige points
    private TokenType bonus; // The token type this card produces
    private Map<TokenType, Integer> cost; // Cost to buy this card
    private String imageUrl; // Can be added later
}
