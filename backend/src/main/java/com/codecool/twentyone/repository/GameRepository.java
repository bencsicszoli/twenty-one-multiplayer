package com.codecool.twentyone.repository;

import com.codecool.twentyone.model.entities.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    @NativeQuery(value = "SELECT * FROM game WHERE player1 = ?1 OR player2 = ?1 OR player3 = ?1 LIMIT 1")
    Optional<Game> findFirstGameByPlayer(@Param("player") String player);

    @NativeQuery(value = "SELECT * FROM game WHERE player1 IS NULL OR player2 IS NULL OR player3 IS NULL LIMIT 1")
    Optional<Game> findFirstGameByMissingPlayer();

    @NativeQuery(value = "SELECT player1 AS active_player FROM game WHERE game_id = ?1 AND player1 IS NOT NULL" +
            "UNION ALL" +
            "SELECT player2 FROM game WHERE game_id = ?1 AND player2 IS NOT NULL" +
            "UNION ALL" +
            "SELECT player3 FROM game WHERE game_id = ?1 and player3 IS NOT NULL")
    List<String> findPlayerNames (@Param("game_id") Long gameId);

}
