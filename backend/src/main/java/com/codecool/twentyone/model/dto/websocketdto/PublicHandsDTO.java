package com.codecool.twentyone.model.dto.websocketdto;

import java.util.List;

public record PublicHandsDTO(List<PublicHandDTO> publicHands, String type) {
}
