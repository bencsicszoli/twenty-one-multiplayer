package com.codecool.twentyone.repository;

import com.codecool.twentyone.model.entities.PlayerHand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerHandRepository extends JpaRepository<PlayerHand, Long> {
}
