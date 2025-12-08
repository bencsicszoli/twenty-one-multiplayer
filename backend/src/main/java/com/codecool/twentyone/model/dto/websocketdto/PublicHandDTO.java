package com.codecool.twentyone.model.dto.websocketdto;

import java.util.List;

public record PublicHandDTO(List<CardDTO> cards, int handValue) {
}

