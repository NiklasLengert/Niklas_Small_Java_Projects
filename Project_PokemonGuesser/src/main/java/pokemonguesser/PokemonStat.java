package pokemonguesser;

public record PokemonStat(String name, int value) {
    public String displayName() {
        return switch (name) {
            case "hp" -> "HP";
            case "attack" -> "ATTACK";
            case "defense" -> "DEFENSE";
            case "special-attack" -> "SP. ATK";
            case "special-defense" -> "SP. DEF";
            case "speed" -> "SPEED";
            default -> name.toUpperCase();
        };
    }
}