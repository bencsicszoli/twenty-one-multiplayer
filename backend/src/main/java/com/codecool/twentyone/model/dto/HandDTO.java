package com.codecool.twentyone.model.dto;

import com.codecool.twentyone.model.entities.PlayerState;

import java.util.List;

public record HandDTO(PlayerState playerState, List<CardDTO> cards, int handValue, String type) {
}
