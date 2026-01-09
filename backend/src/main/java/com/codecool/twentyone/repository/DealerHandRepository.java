package com.codecool.twentyone.repository;

import com.codecool.twentyone.model.entities.DealerHand;
import com.codecool.twentyone.model.entities.PlayerHand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DealerHandRepository extends JpaRepository<DealerHand, Long> {
    Optional<List<DealerHand>> findAllByDealerId(Long dealerId);

    @NativeQuery(value = "SELECT SUM(card_value) FROM dealer_hand WHERE dealer_id = ?1")
    int getHandValue(@Param("dealer_id") Long dealerId);

    @NativeQuery(value = "SELECT COUNT(*) FROM dealer_hand WHERE dealer_id = ?1")
    int getHandSize(@Param("dealer_id") Long dealerId);

    void deleteAllByDealerId(Long dealerId);

    @Modifying
    @NativeQuery(value = "DELETE FROM dealer_hand WHERE dealer_id = ?1 AND card_value = 11")
    void deleteAceByDealerId(@Param("dealer_id") Long dealerId);
}