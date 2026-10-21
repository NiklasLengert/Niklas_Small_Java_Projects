# Pokedex Field Notes

A Java 21 Swing guessing game with an earthy, pixel-console look. Each round
loads a random Pokemon from PokeAPI and shows its type, base stats, height, and
weight. Guess its name in up to five tries.

## Requirements

- JDK 21
- Maven
- Internet connection to load Pokemon data

## Build and run

Open a terminal in this project folder and build the runnable JAR:

```bash
mvn clean package
```

Launch it with:

```bash
java -jar target/pokemon-guesser-1.0-SNAPSHOT.jar
```

Run the game-logic tests with `mvn test`.

Data is provided by [PokeAPI](https://pokeapi.co/). The application caches
successful API responses during the current run and does not include Pokemon
images or other game assets.