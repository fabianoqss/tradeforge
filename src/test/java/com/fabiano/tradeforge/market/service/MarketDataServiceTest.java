package com.fabiano.tradeforge.market.service;

import com.fabiano.tradeforge.market.model.Quote;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class MarketDataServiceTest {

    @Test
    void shouldStoreAndRetrieveLatestQuote() {

        MarketDataService marketDataService = new MarketDataService();

        Quote quote = new Quote(
                "AAPL",
                new BigDecimal("238.50"),
                Instant.now()
        );

        marketDataService.updateQuote(quote);

        Optional<Quote> result = marketDataService.getLatestQuote("AAPL");

        log.info("Latest Quote retrieved: {}", result);

        assertEquals(Optional.of(quote), result);
    }

    @Test
    void shouldUpdateExistingQuote() {

        MarketDataService marketDataService = new MarketDataService();

        Quote firstQuote = new Quote(
                "AAPL",
                new BigDecimal("238.50"),
                Instant.now()
        );

        Quote secondQuote = new Quote(
                "AAPL",
                new BigDecimal("239.10"),
                Instant.now()
        );

        marketDataService.updateQuote(firstQuote);
        marketDataService.updateQuote(secondQuote);

        Optional<Quote> result = marketDataService.getLatestQuote("AAPL");

        log.info("Latest Quote retrieved: {}", result);

        assertEquals(Optional.of(secondQuote), result);
    }


}