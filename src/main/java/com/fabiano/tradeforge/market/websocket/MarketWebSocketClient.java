package com.fabiano.tradeforge.market.websocket;

import com.fabiano.tradeforge.market.model.Quote;
import com.fabiano.tradeforge.market.service.MarketDataService;
import com.twelvedata.client.ws.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Component
public class MarketWebSocketClient {

    private final MarketDataService marketDataService;
    private final TwelvedataWebSocketClient client;



    public MarketWebSocketClient(
            @Value("${market.twelve-data.api-key}") String apiKey,
            MarketDataService marketDataService
    ) {
        this.marketDataService = marketDataService;

        this.client = new TwelvedataWebSocketClient(
                TwelvedataWebSocketOptions.builder()
                        .apiKey(apiKey)
                        .build()
        );

        configureListener();

    }

    private void configureListener() {

        client.addListener(new TwelvedataWebSocketListener() {

            @Override
            public void onPrice(PriceEvent event) {
                Quote quote = new Quote(
                        event.getSymbol(),
                        BigDecimal.valueOf(event.getPrice()),
                        Instant.ofEpochSecond(event.getTimestamp())
                );


                log.info("Quote received: {}", quote);

                marketDataService.updateQuote(quote);

                log.info("Quote updated: {}", quote);
            }

            @Override
            public void onSubscribeStatus(SubscribeStatusEvent event) {
                log.info("Subscribed successfully: {}", event.getSuccess());
                log.info("Subscription failures: {}", event.getFails());
            }

            @Override
            public void onReconnecting(ReconnectingEvent event) {
                System.out.println("Reconnecting...");
            }

            @Override
            public void onError(TwelvedataWebSocketException error) {
                System.err.println(
                        "WebSocket error: " + error.getMessage()
                );
            }
        });
    }

    @PostConstruct
    public void connect() {
        client.connect().join();

        log.info("Connected");

        client.subscribe("AAPL");

    }

    @PreDestroy
    public void disconnect() {
        client.disconnect();
    }

}
