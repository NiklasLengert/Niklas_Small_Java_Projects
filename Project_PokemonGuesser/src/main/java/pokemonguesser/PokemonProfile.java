package pokemonguesser;

import java.util.List;

public record PokemonProfile(
        int id,
        String name,
        String speciesName,
        int heightDecimeters,
        int weightHectograms,
        List<String> types,
        List<PokemonStat> stats) {
    public PokemonProfile {
        types = List.copyOf(types);
        stats = List.copyOf(stats);
    }
}