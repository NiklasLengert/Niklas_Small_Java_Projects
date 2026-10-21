package currencyconverter;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeRate(String base, String quote, BigDecimal rate, LocalDate date) {
}