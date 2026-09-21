package com.fabiano.tradeforge.services;

import com.fabiano.tradeforge.dtos.request.OrderRequestDTO;
import com.fabiano.tradeforge.dtos.response.PositionResponseDTO;
import com.fabiano.tradeforge.entities.Asset;
import com.fabiano.tradeforge.entities.Portfolio;
import com.fabiano.tradeforge.entities.User;
import com.fabiano.tradeforge.enums.OrderSide;
import com.fabiano.tradeforge.enums.OrderStatus;
import com.fabiano.tradeforge.enums.OrderType;
import com.fabiano.tradeforge.market.model.Quote;
import com.fabiano.tradeforge.market.service.MarketDataService;
import com.fabiano.tradeforge.repositories.AssetRepository;
import com.fabiano.tradeforge.repositories.OrderRepository;
import com.fabiano.tradeforge.repositories.PortfolioRepository;
import com.fabiano.tradeforge.services.exceptions.AssetNotTradableException;
import com.fabiano.tradeforge.services.exceptions.InsufficientBalanceException;
import com.fabiano.tradeforge.services.exceptions.QuoteUnavailableException;
import com.fabiano.tradeforge.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderServiceTest {

    private final OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
    private final AssetRepository assetRepository = Mockito.mock(AssetRepository.class);
    private final PortfolioRepository portfolioRepository = Mockito.mock(PortfolioRepository.class);
    private final MarketDataService marketDataService = Mockito.mock(MarketDataService.class);
    private final PositionService positionService = Mockito.mock(PositionService.class);
    private final UserService userService = Mockito.mock(UserService.class);

    private final OrderService orderService = new OrderService(
            orderRepository, assetRepository, portfolioRepository, marketDataService, positionService, userService
    );

    private Portfolio portfolioWithBalance(String balance) {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(1L);
        portfolio.setCashBalance(new BigDecimal(balance));
        Mockito.when(portfolioRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(portfolio));
        return portfolio;
    }

    private Asset tradableAsset() {
        Asset asset = new Asset();
        asset.setSymbol("AAPL");
        asset.setTradable(true);
        return asset;
    }

    private void mockAuthenticatedUser(Portfolio portfolio) {
        User user = new User();
        user.setId(1L);
        user.setPortfolio(portfolio);
        Mockito.when(userService.authenticated()).thenReturn(user);
    }

    @Test
    void placeOrderShouldExecuteMarketBuyAndDebitBalance() {
        Portfolio portfolio = portfolioWithBalance("10000.00");
        Asset asset = tradableAsset();
        mockAuthenticatedUser(portfolio);

        Mockito.when(assetRepository.findBySymbol("AAPL")).thenReturn(Optional.of(asset));
        Mockito.when(marketDataService.getLatestQuote("AAPL"))
                .thenReturn(Optional.of(new Quote("AAPL", new BigDecimal("100.00"), Instant.now())));
        Mockito.when(positionService.applyBuy(Mockito.eq(portfolio), Mockito.eq(asset), Mockito.eq(10), Mockito.eq(new BigDecimal("100.00"))))
                .thenReturn(new PositionResponseDTO("AAPL", 10, new BigDecimal("100.00"), BigDecimal.ZERO));
        Mockito.when(orderRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

        OrderRequestDTO dto = new OrderRequestDTO("AAPL", OrderSide.BUY, OrderType.MARKET, 10, null);

        var result = orderService.placeOrder(dto);

        assertEquals(OrderStatus.EXECUTED, result.status());
        assertEquals("AAPL", result.assetSymbol());

        ArgumentCaptor<Portfolio> portfolioCaptor = ArgumentCaptor.forClass(Portfolio.class);
        Mockito.verify(portfolioRepository).save(portfolioCaptor.capture());
        assertEquals(new BigDecimal("9000.00"), portfolioCaptor.getValue().getCashBalance());

        Mockito.verify(positionService).applyBuy(portfolio, asset, 10, new BigDecimal("100.00"));
    }

    @Test
    void placeOrderShouldThrowWhenBalanceIsInsufficientForBuy() {
        Portfolio portfolio = portfolioWithBalance("500.00");
        Asset asset = tradableAsset();
        mockAuthenticatedUser(portfolio);

        Mockito.when(assetRepository.findBySymbol("AAPL")).thenReturn(Optional.of(asset));
        Mockito.when(marketDataService.getLatestQuote("AAPL"))
                .thenReturn(Optional.of(new Quote("AAPL", new BigDecimal("100.00"), Instant.now())));

        OrderRequestDTO dto = new OrderRequestDTO("AAPL", OrderSide.BUY, OrderType.MARKET, 10, null);

        assertThrows(InsufficientBalanceException.class, () -> orderService.placeOrder(dto));

        Mockito.verify(positionService, Mockito.never()).applyBuy(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void placeOrderShouldExecuteMarketSellAndCreditBalance() {
        Portfolio portfolio = portfolioWithBalance("1000.00");
        Asset asset = tradableAsset();
        mockAuthenticatedUser(portfolio);

        Mockito.when(assetRepository.findBySymbol("AAPL")).thenReturn(Optional.of(asset));
        Mockito.when(marketDataService.getLatestQuote("AAPL"))
                .thenReturn(Optional.of(new Quote("AAPL", new BigDecimal("50.00"), Instant.now())));
        Mockito.when(positionService.applySell(Mockito.eq(portfolio), Mockito.eq(asset), Mockito.eq(5), Mockito.eq(new BigDecimal("50.00"))))
                .thenReturn(new PositionResponseDTO("AAPL", 5, new BigDecimal("40.00"), new BigDecimal("50.00")));
        Mockito.when(orderRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));

        OrderRequestDTO dto = new OrderRequestDTO("AAPL", OrderSide.SELL, OrderType.MARKET, 5, null);

        var result = orderService.placeOrder(dto);

        assertEquals(OrderStatus.EXECUTED, result.status());

        ArgumentCaptor<Portfolio> portfolioCaptor = ArgumentCaptor.forClass(Portfolio.class);
        Mockito.verify(portfolioRepository).save(portfolioCaptor.capture());
        assertEquals(new BigDecimal("1250.00"), portfolioCaptor.getValue().getCashBalance());
    }

    @Test
    void placeOrderShouldThrowWhenAssetIsNotTradable() {
        Portfolio portfolio = portfolioWithBalance("1000.00");
        Asset asset = tradableAsset();
        asset.setTradable(false);
        mockAuthenticatedUser(portfolio);

        Mockito.when(assetRepository.findBySymbol("AAPL")).thenReturn(Optional.of(asset));

        OrderRequestDTO dto = new OrderRequestDTO("AAPL", OrderSide.BUY, OrderType.MARKET, 1, null);

        assertThrows(AssetNotTradableException.class, () -> orderService.placeOrder(dto));
    }

    @Test
    void placeOrderShouldThrowWhenAssetDoesNotExist() {
        Portfolio portfolio = portfolioWithBalance("1000.00");
        mockAuthenticatedUser(portfolio);

        Mockito.when(assetRepository.findBySymbol("GHOST")).thenReturn(Optional.empty());

        OrderRequestDTO dto = new OrderRequestDTO("GHOST", OrderSide.BUY, OrderType.MARKET, 1, null);

        assertThrows(ResourceNotFoundException.class, () -> orderService.placeOrder(dto));
    }

    @Test
    void placeOrderShouldThrowWhenQuoteIsUnavailable() {
        Portfolio portfolio = portfolioWithBalance("1000.00");
        Asset asset = tradableAsset();
        mockAuthenticatedUser(portfolio);

        Mockito.when(assetRepository.findBySymbol("AAPL")).thenReturn(Optional.of(asset));
        Mockito.when(marketDataService.getLatestQuote("AAPL")).thenReturn(Optional.empty());

        OrderRequestDTO dto = new OrderRequestDTO("AAPL", OrderSide.BUY, OrderType.MARKET, 1, null);

        assertThrows(QuoteUnavailableException.class, () -> orderService.placeOrder(dto));
    }
}
