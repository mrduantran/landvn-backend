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
public class Noble {
    private String id;
    private int points;
    private Map<TokenType, Integer> requirements; // Required card bonuses
    private String imageUrl;
}
