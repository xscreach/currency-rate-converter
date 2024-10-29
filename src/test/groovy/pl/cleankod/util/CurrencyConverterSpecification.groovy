package pl.cleankod.util

import spock.lang.Specification

class CurrencyConverterSpecification extends Specification {
    def "should convert currency properly"() {
        when:
        def converted = CurrencyConverter.convert(amount as BigDecimal, rate as BigDecimal)

        then:
        converted == givenValue

        where:
        amount << new BigDecimal("10")
        rate << new BigDecimal("1.5")
        givenValue << new BigDecimal("6.66")
    }
}
