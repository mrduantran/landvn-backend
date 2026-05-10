package com.landvn.backend.util;

import com.landvn.backend.enums.TokenType;
import com.landvn.backend.model.Card;
import com.landvn.backend.model.Noble;

import java.util.*;

public class DeckInitializer {

    private static final Random random = new Random();
    private static final TokenType[] CARD_COLORS = {TokenType.VILLA, TokenType.APARTMENT, TokenType.TOWNHOUSE, TokenType.RESORT, TokenType.LAND};

    public static List<Card> generateLevel1Deck() {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            TokenType bonus = CARD_COLORS[random.nextInt(CARD_COLORS.length)];
            Map<TokenType, Integer> cost = new HashMap<>();
            int totalCost = 3 + random.nextInt(3);
            int points = (totalCost >= 5 && random.nextBoolean()) ? 1 : 0;
            distributeCost(cost, totalCost);
            deck.add(Card.builder().id("L1-" + i).level(1).points(points).bonus(bonus).cost(cost).build());
        }
        return deck;
    }

    public static List<Card> generateLevel2Deck() {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            TokenType bonus = CARD_COLORS[random.nextInt(CARD_COLORS.length)];
            Map<TokenType, Integer> cost = new HashMap<>();
            int totalCost = 5 + random.nextInt(4);
            int points = totalCost >= 8 ? 3 : (totalCost >= 6 ? 2 : 1);
            distributeCost(cost, totalCost);
            deck.add(Card.builder().id("L2-" + i).level(2).points(points).bonus(bonus).cost(cost).build());
        }
        return deck;
    }

    public static List<Card> generateLevel3Deck() {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            TokenType bonus = CARD_COLORS[random.nextInt(CARD_COLORS.length)];
            Map<TokenType, Integer> cost = new HashMap<>();
            int totalCost = 10 + random.nextInt(5);
            int points = totalCost >= 14 ? 5 : (totalCost >= 12 ? 4 : 3);
            distributeCost(cost, totalCost);
            deck.add(Card.builder().id("L3-" + i).level(3).points(points).bonus(bonus).cost(cost).build());
        }
        return deck;
    }

    public static List<Noble> generateNobles(int count) {
        List<Noble> nobles = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<TokenType, Integer> req = new HashMap<>();
            boolean is333 = random.nextBoolean();
            List<TokenType> colors = new ArrayList<>(Arrays.asList(CARD_COLORS));
            Collections.shuffle(colors);
            if (is333) {
                req.put(colors.get(0), 3); req.put(colors.get(1), 3); req.put(colors.get(2), 3);
            } else {
                req.put(colors.get(0), 4); req.put(colors.get(1), 4);
            }
            nobles.add(Noble.builder().id("N-" + i).points(3).requirements(req).build());
        }
        return nobles.subList(0, count);
    }

    private static void distributeCost(Map<TokenType, Integer> cost, int totalCost) {
        List<TokenType> colors = new ArrayList<>(Arrays.asList(CARD_COLORS));
        Collections.shuffle(colors);
        int remaining = totalCost;
        int numColors = 2 + random.nextInt(3);
        for (int i = 0; i < numColors - 1; i++) {
            if (remaining <= 0) break;
            int amount = 1 + random.nextInt(Math.min(remaining, 5));
            cost.put(colors.get(i), amount);
            remaining -= amount;
        }
        if (remaining > 0) cost.put(colors.get(numColors - 1), remaining);
    }
}
