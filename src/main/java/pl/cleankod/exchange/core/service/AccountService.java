package pl.cleankod.exchange.core.service;

import pl.cleankod.exchange.core.domain.Account;
import pl.cleankod.exchange.core.domain.Money;
import pl.cleankod.exchange.core.gateway.AccountRepository;
import pl.cleankod.exchange.core.gateway.CurrencyConversionService;
import pl.cleankod.exchange.core.exception.CurrencyConversionException;

import java.util.Currency;
import java.util.Optional;

public class AccountService {

    private final Currency baseCurrency;
    private final AccountRepository accountRepository;
    private final CurrencyConversionService currencyConverter;

    public AccountService(Currency baseCurrency, AccountRepository accountRepository, CurrencyConversionService currencyConverter) {
        this.baseCurrency = baseCurrency;
        this.accountRepository = accountRepository;
        this.currencyConverter = currencyConverter;
    }

    /**
     * Looks up account based on Account.Id. If currency is specified, account balance is converted to that currency
     *
     * @param accountId      - id of account
     * @param targetCurrency - nullable - currency to convert balance to
     */
    public Optional<Account> getAccount(Account.Id accountId, Currency targetCurrency) {
        return accountRepository.find(accountId)
                .map(account -> convert(account, targetCurrency));
    }

    /**
     * Looks up account based on Account.Number. If currency is specified, account balance is converted to that currency
     *
     * @param accountNumber  - number of account
     * @param targetCurrency - nullable - currency to convert balance to
     */
    public Optional<Account> getAccount(Account.Number accountNumber, Currency targetCurrency) {
        return accountRepository.find(accountNumber)
                .map(account -> convert(account, targetCurrency));
    }

    private Account convert(Account account, Currency targetCurrency) {
        return Optional.ofNullable(targetCurrency)
                .map(c -> new Account(account.id(), account.number(), convert(account.balance(), targetCurrency)))
                .orElse(account);
    }

    private Money convert(Money money, Currency targetCurrency) {
        if (!baseCurrency.equals(targetCurrency)) {
            return currencyConverter.convert(money, targetCurrency);
        }

        if (!money.currency().equals(targetCurrency)) {
            throw new CurrencyConversionException(money.currency(), targetCurrency);
        }

        return money;
    }
}
