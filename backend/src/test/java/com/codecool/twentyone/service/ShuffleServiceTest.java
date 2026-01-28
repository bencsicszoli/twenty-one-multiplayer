package com.codecool.twentyone.service;

import com.codecool.twentyone.model.entities.Card;
import com.codecool.twentyone.model.entities.FakeDeck;
import com.codecool.twentyone.repository.CardRepository;
import com.codecool.twentyone.repository.FakeDeckRepository;
import com.codecool.twentyone.repository.ShuffleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShuffleServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private ShuffleRepository shuffleRepository;

    @Mock
    private Random random;

    @Mock
    private FakeDeckRepository fakeDeckRepository;
    private ShuffleService shuffleService;

    @Test
    void addShuffledDeckShouldSaveRandomDeck_when_gameIdIsValid() {
        List<Card >testCards = IntStream.rangeClosed(1, 32)
                .mapToObj(i -> {
                    Card card = new Card();
                    card.setId(i);
                    return card;
                }).collect(Collectors.toList());
        shuffleService = new ShuffleService(shuffleRepository, cardRepository, random, fakeDeckRepository);
        Long gameId = 5L;
        when(cardRepository.findAll()).thenReturn(testCards);
        when(random.nextInt(anyInt())).thenReturn(0);

        shuffleService.addShuffledDeck(gameId);

        verify(shuffleRepository, times(1)).deleteByGameId(gameId);
    }

    @Test
    void useFakeDeck_shouldSaveFakeDeckToShuffleRepository_when_gameIdIsValid() {
        List<FakeDeck> fakeDeck = LongStream.rangeClosed(1, 32)
                .mapToObj(i -> {
                    FakeDeck card = new FakeDeck();
                    card.setId(i);
                    return card;
                }).collect(Collectors.toList());
        shuffleService = new ShuffleService(shuffleRepository, cardRepository, random, fakeDeckRepository);
        Long gameId = 5L;
        when(fakeDeckRepository.findAll()).thenReturn(fakeDeck);

        shuffleService.useFakeDeck(gameId);

        verify(shuffleRepository, times(1)).saveAll(anyList());
    }
}
