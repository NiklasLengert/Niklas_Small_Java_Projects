package pokemonguesser;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PokemonGuessingRoundTest {
    private PokemonProfile pikachu() {
        return new PokemonProfile(
                25,
                "pikachu",
                "pikachu",
                4,
                60,
                List.of("electric"),
                List.of(new PokemonStat("hp", 35)));
    }

    @Test
    void acceptsCaseAndPunctuationVariations() {
        PokemonGuessingRound round = new PokemonGuessingRound(pikachu());

        assertEquals(PokemonGuessingRound.Result.CORRECT, round.submitGuess("Pika-chu!"));
        assertTrue(round.isSolved());
        assertTrue(round.isFinished());
    }

    @Test
    void acceptsSpeciesNameForDefaultVariety() {
        PokemonProfile profile = new PokemonProfile(
                413,
                "wormadam-plant",
                "wormadam",
                5,
                65,
                List.of("bug", "grass"),
                List.of(new PokemonStat("hp", 60)));
        PokemonGuessingRound round = new PokemonGuessingRound(profile);

        assertEquals(PokemonGuessingRound.Result.CORRECT, round.submitGuess("Wormadam"));
    }

    @Test
    void decreasesGuessesAndRevealsAnswerAfterFiveMisses() {
        PokemonGuessingRound round = new PokemonGuessingRound(pikachu());

        for (int guessNumber = 1; guessNumber < PokemonGuessingRound.MAX_GUESSES; guessNumber++) {
            assertEquals(PokemonGuessingRound.Result.INCORRECT, round.submitGuess("bulbasaur"));
        }
        assertEquals(1, round.getGuessesRemaining());
        assertEquals(PokemonGuessingRound.Result.REVEALED, round.submitGuess("bulbasaur"));
        assertTrue(round.isFinished());
        assertFalse(round.isSolved());
        assertEquals(PokemonGuessingRound.Result.ROUND_FINISHED, round.submitGuess("pikachu"));
    }

    @Test
    void rejectsBlankGuesses() {
        PokemonGuessingRound round = new PokemonGuessingRound(pikachu());

        assertThrows(IllegalArgumentException.class, () -> round.submitGuess("   "));
        assertEquals(PokemonGuessingRound.MAX_GUESSES, round.getGuessesRemaining());
    }
}