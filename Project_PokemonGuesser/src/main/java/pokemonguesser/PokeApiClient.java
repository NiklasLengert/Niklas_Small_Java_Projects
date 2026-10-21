package pokemonguesser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

public class PokeApiClient {
    private static final String API_ROOT = "https://pokeapi.co/api/v2";
    private static final URI SPECIES_LIST_URI = URI.create(API_ROOT + "/pokemon-species?limit=2000");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentMap<URI, JsonNode> responseCache = new ConcurrentHashMap<>();

    public CompletableFuture<List<PokemonSpecies>> fetchSpecies() {
        return fetchJson(SPECIES_LIST_URI).thenApply(this::parseSpeciesList);
    }

    public CompletableFuture<PokemonProfile> fetchRandomPokemon(Integer excludedSpeciesId) {
        return fetchSpecies().thenCompose(species -> {
            List<PokemonSpecies> choices = species.stream()
                    .filter(entry -> excludedSpeciesId == null || entry.id() != excludedSpeciesId)
                    .toList();
            if (choices.isEmpty()) {
                return CompletableFuture.failedFuture(
                        new IOException("No Pokemon are available for a new round."));
            }

            int selectedIndex = ThreadLocalRandom.current().nextInt(choices.size());
            return fetchPokemon(choices.get(selectedIndex));
        });
    }

    private CompletableFuture<PokemonProfile> fetchPokemon(PokemonSpecies species) {
        return fetchJson(species.url()).thenCompose(speciesData -> {
            JsonNode varieties = speciesData.path("varieties");
            URI defaultPokemonUri = null;
            if (varieties.isArray()) {
                for (JsonNode variety : varieties) {
                    if (variety.path("is_default").asBoolean()) {
                        String url = variety.path("pokemon").path("url").asText("");
                        if (!url.isBlank()) {
                            defaultPokemonUri = URI.create(url);
                        }
                        break;
                    }
                }
            }

            if (defaultPokemonUri == null) {
                return CompletableFuture.failedFuture(
                        new IOException("Pokemon species did not include a default variety."));
            }

            URI pokemonUri = defaultPokemonUri;
            return fetchJson(pokemonUri).thenApply(pokemonData -> parsePokemon(species, pokemonData));
        });
    }

    private CompletableFuture<JsonNode> fetchJson(URI uri) {
        JsonNode cached = responseCache.get(uri);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        throw new CompletionException(new IOException(
                                "PokeAPI returned HTTP " + response.statusCode() + "."));
                    }
                    try {
                        JsonNode data = objectMapper.readTree(response.body());
                        responseCache.putIfAbsent(uri, data);
                        return data;
                    } catch (JsonProcessingException exception) {
                        throw new CompletionException(new IOException("Could not read PokeAPI response.", exception));
                    }
                });
    }

    private List<PokemonSpecies> parseSpeciesList(JsonNode response) {
        JsonNode results = response.path("results");
        if (!results.isArray()) {
            throw new CompletionException(new IOException("PokeAPI returned an invalid species list."));
        }

        List<PokemonSpecies> species = new ArrayList<>();
        for (JsonNode result : results) {
            String name = result.path("name").asText("");
            String url = result.path("url").asText("");
            if (name.isBlank() || url.isBlank()) {
                continue;
            }

            URI resource = URI.create(url);
            String path = resource.getPath();
            String[] segments = path.split("/");
            try {
                int id = Integer.parseInt(segments[segments.length - 1]);
                species.add(new PokemonSpecies(id, name, resource));
            } catch (NumberFormatException ignored) {
            }
        }

        if (species.isEmpty()) {
            throw new CompletionException(new IOException("PokeAPI returned no Pokemon species."));
        }
        return List.copyOf(species);
    }

    private PokemonProfile parsePokemon(PokemonSpecies species, JsonNode data) {
        int id = data.path("id").asInt(-1);
        int height = data.path("height").asInt(-1);
        int weight = data.path("weight").asInt(-1);
        JsonNode statsData = data.path("stats");
        JsonNode typesData = data.path("types");
        if (id < 1 || height < 0 || weight < 0 || !statsData.isArray() || !typesData.isArray()) {
            throw new CompletionException(new IOException("PokeAPI returned incomplete Pokemon data."));
        }

        List<PokemonStat> stats = new ArrayList<>();
        for (JsonNode stat : statsData) {
            String name = stat.path("stat").path("name").asText("");
            int value = stat.path("base_stat").asInt(-1);
            if (!name.isBlank() && value >= 0) {
                stats.add(new PokemonStat(name, value));
            }
        }

        List<String> types = new ArrayList<>();
        for (JsonNode type : typesData) {
            String name = type.path("type").path("name").asText("");
            if (!name.isBlank()) {
                types.add(name);
            }
        }

        return new PokemonProfile(
                id,
                data.path("name").asText(species.name()),
                species.name(),
                height,
                weight,
                types,
                stats);
    }
}