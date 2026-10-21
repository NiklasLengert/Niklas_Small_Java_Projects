package currencyconverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class CurrencyConverter {
    public BigDecimal convert(BigDecimal amount, BigDecimal rate) {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(rate, "rate");

        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Amount cannot be negative.");
        }
        if (rate.signum() <= 0) {
            throw new IllegalArgumentException("Exchange rate must be greater than zero.");
        }

        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}