package com.codecool.twentyone.repository;

import com.codecool.twentyone.model.entities.PlayerHand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerHandRepository extends JpaRepository<PlayerHand, Long> {
    Optional<List<PlayerHand>> getAllByPlayerId(Long playerId);
}
