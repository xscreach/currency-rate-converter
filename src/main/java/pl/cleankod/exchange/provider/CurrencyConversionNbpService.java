package pl.cleankod.exchange.provider;

import pl.cleankod.exchange.core.domain.Money;
import pl.cleankod.exchange.core.gateway.CurrencyConversionService;
import pl.cleankod.exchange.provider.nbp.ExchangeRatesNbpClient;
import pl.cleankod.exchange.provider.nbp.model.RateWrapper;
import pl.cleankod.util.CurrencyConverter;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CurrencyConversionNbpService implements CurrencyConversionService {
    private final ExchangeRatesNbpClient exchangeRatesNbpClient;
    private final ExecutorService executorService;
    private final long maxWaitMs;

    public CurrencyConversionNbpService(ExchangeRatesNbpClient exchangeRatesNbpClient, ExecutorService executorService, long maxWaitMs) {
        this.exchangeRatesNbpClient = exchangeRatesNbpClient;
        this.executorService = executorService;
        this.maxWaitMs = maxWaitMs;
    }

    @Override
    public Money convert(Money money, Currency targetCurrency) {
        BigDecimal midRate = getExchangeRate(targetCurrency);
        BigDecimal calculatedRate = CurrencyConverter.convert(money.amount(), midRate);
        return new Money(calculatedRate, targetCurrency);
    }

    private BigDecimal getExchangeRate(Currency targetCurrency) {
        try {
            Future<BigDecimal> future = executorService.submit(() -> {
                RateWrapper rateWrapper = exchangeRatesNbpClient.fetch("A", targetCurrency.getCurrencyCode());
                return rateWrapper.rates().get(0).mid();
            });
            return future.get(maxWaitMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // waiting interrupted - most probably by application shutdown
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Could not contact NBP", e);
        } catch (TimeoutException e) {
            throw new IllegalStateException("Could not contact NBP within %d(ms)".formatted(maxWaitMs), e);
        }
    }
}
