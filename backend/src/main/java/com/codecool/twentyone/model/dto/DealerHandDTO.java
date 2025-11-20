package com.codecool.twentyone.model.dto;

import java.util.List;

public record DealerHandDTO(List<CardDTO> cards, int handValue, String type) {
}
