package currencyconverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class FrankfurterClient {
    private static final String API_ROOT = "https://api.frankfurter.dev/v2";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CompletableFuture<List<Currency>> fetchCurrencies() {
        return get(URI.create(API_ROOT + "/currencies"))
                .thenApply(this::parseCurrencies);
    }

    public CompletableFuture<ExchangeRate> fetchRate(String base, String quote) {
        if (!isCurrencyCode(base) || !isCurrencyCode(quote)) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Currency codes must contain three letters."));
        }

        String path = "/rate/" + base.toLowerCase(Locale.ROOT)
                + "/" + quote.toLowerCase(Locale.ROOT);
        return get(URI.create(API_ROOT + path))
                .thenApply(this::parseRate);
    }

    private CompletableFuture<String> get(URI uri) {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(12))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        throw new CompletionException(new IOException(
                                errorMessage(response.statusCode(), response.body())));
                    }
                    return response.body();
                });
    }

    private List<Currency> parseCurrencies(String body) {
        try {
            JsonNode rows = objectMapper.readTree(body);
            if (!rows.isArray()) {
                throw new IOException("Currency service returned an unexpected response.");
            }

            List<Currency> currencies = new ArrayList<>();
            for (JsonNode row : rows) {
                String code = row.path("iso_code").asText("");
                String name = row.path("name").asText("");
                if (isCurrencyCode(code) && !name.isBlank()) {
                    currencies.add(new Currency(code, name));
                }
            }

            currencies.sort(Comparator.comparing(Currency::code));
            if (currencies.isEmpty()) {
                throw new IOException("Currency service returned no currencies.");
            }
            return List.copyOf(currencies);
        } catch (JsonProcessingException exception) {
            throw new CompletionException(new IOException("Could not read currency list.", exception));
        } catch (IOException exception) {
            throw new CompletionException(exception);
        }
    }

    private ExchangeRate parseRate(String body) {
        try {
            JsonNode response = objectMapper.readTree(body);
            String base = response.path("base").asText();
            String quote = response.path("quote").asText();
            JsonNode rateNode = response.path("rate");
            if (!isCurrencyCode(base) || !isCurrencyCode(quote) || !rateNode.isNumber()) {
                throw new IOException("Rate service returned an unexpected response.");
            }

            BigDecimal rate = rateNode.decimalValue();
            LocalDate date = LocalDate.parse(response.path("date").asText());
            return new ExchangeRate(base, quote, rate, date);
        } catch (JsonProcessingException | DateTimeParseException exception) {
            throw new CompletionException(new IOException("Could not read exchange rate.", exception));
        } catch (IOException exception) {
            throw new CompletionException(exception);
        }
    }

    private String errorMessage(int statusCode, String body) {
        try {
            JsonNode response = objectMapper.readTree(body);
            String message = response.path("message").asText();
            if (!message.isBlank()) {
                return message;
            }
        } catch (JsonProcessingException ignored) {
        }
        return "Rate service returned HTTP " + statusCode + ".";
    }

    private static boolean isCurrencyCode(String code) {
        return code != null && code.matches("[A-Z]{3}");
    }
}