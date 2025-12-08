package com.codecool.twentyone.model.dto.restdto;

import java.util.List;

public record JwtResponseDTO(String jwt, String playerName, List<String> roles) {
}
