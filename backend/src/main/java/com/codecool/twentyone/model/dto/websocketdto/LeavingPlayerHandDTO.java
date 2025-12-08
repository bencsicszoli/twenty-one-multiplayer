package com.codecool.twentyone.model.dto.websocketdto;

import java.util.List;

public record LeavingPlayerHandDTO(String playerName, List<CardDTO> cards, int handValue, String type) {
}
