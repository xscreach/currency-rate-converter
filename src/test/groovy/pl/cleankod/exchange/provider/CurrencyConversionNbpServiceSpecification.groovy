package pl.cleankod.exchange.provider

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.apache.http.HttpResponse
import pl.cleankod.BaseApplicationSpecification

class CurrencyConversionNbpServiceSpecification extends BaseApplicationSpecification {
    private static WireMockServer wireMockServer = new WireMockServer(
            WireMockConfiguration.options().port(8081)
    )

    def setupSpec() {
        wireMockServer.start()
    }

    def cleanupSpec() {
        wireMockServer.stop()
    }

    def "should fail properly with no, but timely response from NBP"() {
        given:
        def accountId = "fa07c538-8ce4-11ec-9ad5-4f5a625cd744"
        def currency = "EUR"

        when:
        HttpResponse response = getResponse("/accounts/${accountId}?currency=${currency}")

        then:
        response.getStatusLine().getStatusCode() == 400
        transformError(response).message() == "Could not contact NBP"
    }

    def "should fail properly with untimely response from NBP"() {
        given:
        def accountId = "fa07c538-8ce4-11ec-9ad5-4f5a625cd744"
        def currency = "EUR"
        def body = "{\"table\":\"A\",\"currency\":\"euro\",\"code\":\"EUR\",\"rates\":[{\"no\":\"026/A/NBP/2022\",\"effectiveDate\":\"2022-02-08\",\"mid\":4.5452}]}"
        wireMockServer.stubFor(
                WireMock.get("/exchangerates/rates/A/EUR/2022-02-08")
                        .willReturn(WireMock.ok(body).withFixedDelay(1000))
        )

        when:
        HttpResponse response = getResponse("/accounts/${accountId}?currency=${currency}")

        then:
        response.getStatusLine().getStatusCode() == 400
        transformError(response).message() == "Could not contact NBP within 500(ms)"
    }
}
