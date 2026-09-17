package com.fabiano.tradeforge.market.client;

import com.fabiano.tradeforge.market.model.Quote;

public interface MarketDataClient {

    Quote getQuote(String symbol);
}
