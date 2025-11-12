package com.codecool.twentyone.repository;

import com.codecool.twentyone.model.entities.Card;
import com.codecool.twentyone.model.entities.Shuffle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ShuffleRepository extends JpaRepository<Shuffle, Long> {

    @Query("SELECT s.card FROM Shuffle s WHERE s.gameId = :gameId AND s.cardOrder = :cardOrder")
    Optional<Card> findCardByGameIdAndCardOrder(@Param("gameId") Long gameId, @Param("cardOrder") int cardOrder);

    void deleteByGameId(Long gameId);
}

