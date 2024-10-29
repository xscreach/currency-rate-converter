package pl.cleankod.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public interface CurrencyConverter {
    static BigDecimal convert(BigDecimal amount, BigDecimal rate) {
        return amount.divide(rate, 2, RoundingMode.DOWN);
    }
}
