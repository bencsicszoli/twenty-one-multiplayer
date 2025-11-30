package com.codecool.twentyone.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class FakeDeck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long gameId;
    private int cardOrder;

    @ManyToOne
    @JoinColumn(name = "card_id")
    private Card card;

    public FakeDeck(Card card, Long gameId, int cardOrder) {
        this.card = card;
        this.gameId = gameId;
        this.cardOrder = cardOrder;
    }

    public FakeDeck() {
    }
}

