package com.codecool.twentyone.service;

import com.codecool.twentyone.model.entities.Card;
import com.codecool.twentyone.model.entities.FakeDeck;
import com.codecool.twentyone.model.entities.Shuffle;
import com.codecool.twentyone.repository.CardRepository;
import com.codecool.twentyone.repository.FakeDeckRepository;
import com.codecool.twentyone.repository.ShuffleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ShuffleService {
    private final ShuffleRepository shuffleRepository;
    private final CardRepository cardRepository;
    private final Random random;
    private final FakeDeckRepository fakeDeckRepository;

    public ShuffleService(ShuffleRepository shuffleRepository, CardRepository cardRepository, Random random, FakeDeckRepository fakeDeckRepository) {
        this.shuffleRepository = shuffleRepository;
        this.cardRepository = cardRepository;
        this.random = random;
        this.fakeDeckRepository = fakeDeckRepository;
    }

    //@Transactional
    public void addShuffledDeck(Long gameId) {
        shuffleRepository.deleteByGameId(gameId);
        List<Integer> shuffledCardIndexes = getShuffledCardIndexes();
        List<Shuffle> cardsToSave = new ArrayList<>();
        List<Card> allCards = cardRepository.findAll();
        for (int i = 0; i < shuffledCardIndexes.size(); i++) {
            int orderInIndexes = shuffledCardIndexes.get(i);
            Card card = allCards.stream().filter(e -> e.getId() == orderInIndexes).findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Card not found"));
            cardsToSave.add(new Shuffle(card, gameId, i + 1));
        }
        shuffleRepository.saveAll(cardsToSave);
    }

    private List<Integer> getShuffledCardIndexes() {
        List<Integer> cardIndexes = IntStream.rangeClosed(1, 32)
                .boxed()
                .collect(Collectors.toList());
        Collections.shuffle(cardIndexes, random);
        return cardIndexes;
    }

    public void useFakeDeck(Long gameId) {
        List<FakeDeck> cardsToSave = fakeDeckRepository.findAll();
        List<Shuffle> fakeCards = new ArrayList<>();
        for (FakeDeck fakeDeck : cardsToSave) {
            fakeCards.add(new Shuffle(fakeDeck.getCard(), gameId, fakeDeck.getCardOrder()));
        }
        shuffleRepository.saveAll(fakeCards);
    }
}
