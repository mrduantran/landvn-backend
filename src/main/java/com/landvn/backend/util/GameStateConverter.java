package com.landvn.backend.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.landvn.backend.model.GameState;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class GameStateConverter implements AttributeConverter<GameState, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(GameState gameState) {
        if (gameState == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(gameState);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting GameState to JSON", e);
        }
    }

    @Override
    public GameState convertToEntityAttribute(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, GameState.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error reading GameState from JSON", e);
        }
    }
}
