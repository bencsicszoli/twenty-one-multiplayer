package com.codecool.twentyone.repository;

import com.codecool.twentyone.model.entities.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DealerRepository extends JpaRepository<Dealer, Long> {
    //Optional<Dealer> findById(Long id);
    //int findCardNumberById(Long id);

    @NativeQuery(value = "SELECT card_number FROM dealer WHERE id = ?1")
    int findCardNumberById(@Param("card_number") Long id);

    @Modifying
    @NativeQuery(value = "UPDATE dealer SET card_number = 0 WHERE id = ?1")
    void setCardNumberById(@Param("id") Long id);
}
