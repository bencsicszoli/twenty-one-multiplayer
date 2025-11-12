package com.codecool.twentyone.repository;

import com.codecool.twentyone.model.entities.DealerHand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DealerHandRepository extends JpaRepository<DealerHand, Long> {
    Optional<List<DealerHand>> getAllByDealerId(Long dealerId);
}