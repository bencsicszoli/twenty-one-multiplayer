package com.codecool.twentyone.repository;

import com.codecool.twentyone.model.entities.Game;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    @NativeQuery(value = "SELECT * FROM game WHERE player1 = ?1 OR player2 = ?1 OR player3 = ?1 OR player4 = ?1 LIMIT 1")
    Optional<Game> findFirstGameByPlayer(@Param("player") String player);

    @NativeQuery(value = "SELECT * FROM game WHERE player1 IS NULL OR player2 IS NULL OR player3 IS NULL OR player4 IS NULL LIMIT 1")
    Optional<Game> findFirstGameByMissingPlayer();

    @NativeQuery(value = "SELECT player1 AS active_player FROM game WHERE game_id = ?1 AND player1 IS NOT NULL" +
            "UNION ALL" +
            "SELECT player2 FROM game WHERE game_id = ?1 AND player2 IS NOT NULL" +
            "UNION ALL" +
            "SELECT player3 FROM game WHERE game_id = ?1 and player3 IS NOT NULL")
    List<String> findPlayerNames (@Param("game_id") Long gameId);

    @NativeQuery (value = "SELECT CASE WHEN player1 = ?1 THEN 'player1' WHEN player2 = ?1 THEN 'player2' WHEN player3 = ?1 THEN 'player3' WHEN player4 = ?1 THEN 'player4' END AS found_column FROM game WHERE ?1 IN (player1, player2, player3, player4)")
    String findColumnByPlayerName(String playerName);





}
