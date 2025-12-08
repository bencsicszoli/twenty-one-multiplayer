package com.codecool.twentyone.model.dto.websocketdto;

import java.util.List;

public record DealerHandDTO(List<CardDTO> cards, int handValue) {
}
