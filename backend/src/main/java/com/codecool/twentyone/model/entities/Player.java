package com.codecool.twentyone.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import java.util.Set;

@Entity
@Getter
@Setter

public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerName;
    private String password;
    private String email;
    private int games = 0;
    private int wins = 0;
    private int losses = 0;
    private int balance = 100;
    private int cardNumber = 0;
    private int pot = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_roles", joinColumns = @JoinColumn(name = "player_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<Role> roles;

    @Enumerated(EnumType.STRING)
    @Column(name="state")
    private PlayerState playerState = PlayerState.WAITING_CARD;
}
