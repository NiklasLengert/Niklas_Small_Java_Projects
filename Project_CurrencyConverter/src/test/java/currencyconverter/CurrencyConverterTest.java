package currencyconverter;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrencyConverterTest {
    private final CurrencyConverter converter = new CurrencyConverter();

    @Test
    void convertsAndRoundsToMinorUnits() {
        BigDecimal result = converter.convert(new BigDecimal("12.50"), new BigDecimal("0.8906"));

        assertEquals(new BigDecimal("11.13"), result);
    }

    @Test
    void acceptsZeroAmount() {
        assertEquals(new BigDecimal("0.00"), converter.convert(BigDecimal.ZERO, BigDecimal.ONE));
    }

    @Test
    void rejectsNegativeAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.convert(new BigDecimal("-1"), BigDecimal.ONE));
    }

    @Test
    void rejectsNonPositiveRate() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.convert(BigDecimal.ONE, BigDecimal.ZERO));
    }
}