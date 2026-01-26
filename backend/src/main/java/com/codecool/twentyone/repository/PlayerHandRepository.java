package com.codecool.twentyone.repository;

import com.codecool.twentyone.model.entities.PlayerCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerHandRepository extends JpaRepository<PlayerCard, Long> {
    Optional<List<PlayerCard>> findAllByPlayerId(Long playerId);

    @NativeQuery(value = "SELECT SUM(card_value) FROM player_hand WHERE player_id = ?1")
    int getHandValue(@Param("player_id") Long playerId);

    @NativeQuery(value = "SELECT COUNT(*) FROM player_hand WHERE player_id = ?1")
    int getHandSize(@Param("player_id") Long playerId);

    void deleteAllByPlayerId(Long playerId);

    @Modifying
    @NativeQuery(value = "DELETE FROM player_hand WHERE card_value = 11 AND player_id = ?1")
    void deleteAceFromHand(@Param("player_id") Long playerId);
}
