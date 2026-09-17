package com.fabiano.tradeforge.market.service;

import com.fabiano.tradeforge.market.model.Quote;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MarketDataService {

    private final Map<String, Quote> latestQuotes = new ConcurrentHashMap<>();


    public void updateQuote(Quote quote) {
        latestQuotes.put(quote.symbol(),  quote);
    }

    public Optional<Quote> getLatestQuote(String symbol) {
        return Optional.ofNullable(latestQuotes.get(symbol));
    }
}
