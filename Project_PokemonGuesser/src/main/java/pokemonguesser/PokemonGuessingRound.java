package pokemonguesser;

import java.text.Normalizer;
import java.util.Locale;

public class PokemonGuessingRound {
    public static final int MAX_GUESSES = 5;

    public enum Result {
        CORRECT,
        INCORRECT,
        REVEALED,
        ROUND_FINISHED
    }

    private final PokemonProfile answer;
    private int incorrectGuesses;
    private boolean solved;

    public PokemonGuessingRound(PokemonProfile answer) {
        this.answer = answer;
    }

    public PokemonProfile getAnswer() {
        return answer;
    }

    public int getGuessesRemaining() {
        return MAX_GUESSES - incorrectGuesses;
    }

    public boolean isFinished() {
        return solved || incorrectGuesses >= MAX_GUESSES;
    }

    public boolean isSolved() {
        return solved;
    }

    public Result submitGuess(String guess) {
        if (isFinished()) {
            return Result.ROUND_FINISHED;
        }
        if (normalizeName(guess).isEmpty()) {
            throw new IllegalArgumentException("Guess cannot be blank.");
        }

        String normalizedGuess = normalizeName(guess);
        if (normalizedGuess.equals(normalizeName(answer.name()))
                || normalizedGuess.equals(normalizeName(answer.speciesName()))) {
            solved = true;
            return Result.CORRECT;
        }

        incorrectGuesses++;
        return isFinished() ? Result.REVEALED : Result.INCORRECT;
    }

    static String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        String genderNormalized = name.toLowerCase(Locale.ROOT)
                .replace("♀", "f")
                .replace("♂", "m");
        String decomposed = Normalizer.normalize(genderNormalized, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "").replaceAll("[^a-z0-9]", "");
    }
}