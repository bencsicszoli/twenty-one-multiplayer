package com.codecool.twentyone.repository;

import com.codecool.twentyone.model.entities.PlayerHand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerHandRepository extends JpaRepository<PlayerHand, Long> {
    Optional<List<PlayerHand>> getAllByPlayerId(Long playerId);

    @NativeQuery(value = "SELECT SUM(card_value) FROM player_hand WHERE player_id = ?1")
    Integer getHandValue(@Param("player_id") Long playerId);
}
