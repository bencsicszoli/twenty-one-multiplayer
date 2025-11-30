package com.codecool.twentyone.repository;

import com.codecool.twentyone.model.entities.FakeDeck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FakeDeckRepository extends JpaRepository<FakeDeck, Long> {
}
