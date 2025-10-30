package com.codecool.twentyone.repository;

import com.codecool.twentyone.model.entities.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByPlayerName(String playerName);
    boolean existsByPlayerName(String playerName);
    boolean existsByEmail(String email);
}
