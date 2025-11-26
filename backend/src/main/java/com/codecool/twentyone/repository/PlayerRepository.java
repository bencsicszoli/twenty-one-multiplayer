package com.codecool.twentyone.repository;

import com.codecool.twentyone.model.entities.Player;
import com.codecool.twentyone.model.entities.PlayerState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByPlayerName(String playerName);
    boolean existsByPlayerName(String playerName);
    boolean existsByEmail(String email);

    //PlayerState findStateByPlayerName(String playerName);


    @NativeQuery(value = "SELECT card_number FROM player WHERE player_name = ?1")
    int cardNumberByPlayerName(@Param("player_name") String playerName);

    @NativeQuery(value = "UPDATE player SET state='WAITING_CARD' WHERE id = ?1")
    void updatePlayerState(@Param("id") Long id);

    @NativeQuery(value = "UPDATE player SET card_number=0 WHERE player_name = ?1")
    void resetCardNumber(@Param("player_name") String playerName);

    @NativeQuery(value = "SELECT state FROM player WHERE player_name = ?1")
    PlayerState getPlayerStateByPlayerName(@Param("player_name") String playerName);

    @NativeQuery(value = "SELECT balance FROM player WHERE player_name = ?1")
    int getBalanceByPlayerName(@Param("player_name") String playerName);
}
