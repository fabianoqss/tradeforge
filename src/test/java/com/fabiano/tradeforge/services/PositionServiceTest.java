package com.fabiano.tradeforge.services;

import com.fabiano.tradeforge.entities.Asset;
import com.fabiano.tradeforge.entities.Portfolio;
import com.fabiano.tradeforge.entities.Position;
import com.fabiano.tradeforge.repositories.PositionRepository;
import com.fabiano.tradeforge.services.exceptions.InsufficientPositionException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PositionServiceTest {

    private final Portfolio portfolio = new Portfolio();
    private final Asset asset = new Asset();

    @Test
    void applyBuyShouldCreateNewPositionWhenNoneExists() {
        PositionRepository positionRepository = Mockito.mock(PositionRepository.class);
        PositionService positionService = new PositionService(positionRepository);

        Mockito.when(positionRepository.findByPortfolioAndAsset(portfolio, asset))
                .thenReturn(Optional.empty());
        Mockito.when(positionRepository.save(Mockito.any(Position.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var dto = positionService.applyBuy(portfolio, asset, 10, new BigDecimal("100.00"));

        assertEquals(10, dto.quantity());
        assertEquals(new BigDecimal("100.00000000"), dto.averagePrice());
        assertEquals(BigDecimal.ZERO, dto.realizedPnl());
    }

    @Test
    void applyBuyShouldRecalculateWeightedAveragePrice() {
        PositionRepository positionRepository = Mockito.mock(PositionRepository.class);
        PositionService positionService = new PositionService(positionRepository);

        Position existing = new Position();
        existing.setPortfolio(portfolio);
        existing.setAsset(asset);
        existing.setQuantity(10);
        existing.setAveragePrice(new BigDecimal("100.00"));
        existing.setRealizedPnl(BigDecimal.ZERO);

        Mockito.when(positionRepository.findByPortfolioAndAsset(portfolio, asset))
                .thenReturn(Optional.of(existing));
        Mockito.when(positionRepository.save(Mockito.any(Position.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 10 @ 100 + 10 @ 200 -> 20 @ 150
        var dto = positionService.applyBuy(portfolio, asset, 10, new BigDecimal("200.00"));

        assertEquals(20, dto.quantity());
        assertEquals(new BigDecimal("150.00000000"), dto.averagePrice());
    }

    @Test
    void applySellShouldReduceQuantityAndCalculateRealizedPnl() {
        PositionRepository positionRepository = Mockito.mock(PositionRepository.class);
        PositionService positionService = new PositionService(positionRepository);

        Position existing = new Position();
        existing.setPortfolio(portfolio);
        existing.setAsset(asset);
        existing.setQuantity(10);
        existing.setAveragePrice(new BigDecimal("100.00"));
        existing.setRealizedPnl(BigDecimal.ZERO);

        Mockito.when(positionRepository.findByPortfolioAndAsset(portfolio, asset))
                .thenReturn(Optional.of(existing));
        Mockito.when(positionRepository.save(Mockito.any(Position.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // sell 4 @ 150 when avg cost is 100 -> realized gain of 200
        var dto = positionService.applySell(portfolio, asset, 4, new BigDecimal("150.00"));

        assertEquals(6, dto.quantity());
        assertEquals(new BigDecimal("200.00"), dto.realizedPnl());
    }

    @Test
    void applySellShouldThrowWhenPositionDoesNotExist() {
        PositionRepository positionRepository = Mockito.mock(PositionRepository.class);
        PositionService positionService = new PositionService(positionRepository);

        Mockito.when(positionRepository.findByPortfolioAndAsset(portfolio, asset))
                .thenReturn(Optional.empty());

        assertThrows(InsufficientPositionException.class,
                () -> positionService.applySell(portfolio, asset, 1, new BigDecimal("100.00")));
    }

    @Test
    void applySellShouldThrowWhenQuantityIsInsufficient() {
        PositionRepository positionRepository = Mockito.mock(PositionRepository.class);
        PositionService positionService = new PositionService(positionRepository);

        Position existing = new Position();
        existing.setPortfolio(portfolio);
        existing.setAsset(asset);
        existing.setQuantity(3);
        existing.setAveragePrice(new BigDecimal("100.00"));
        existing.setRealizedPnl(BigDecimal.ZERO);

        Mockito.when(positionRepository.findByPortfolioAndAsset(portfolio, asset))
                .thenReturn(Optional.of(existing));

        assertThrows(InsufficientPositionException.class,
                () -> positionService.applySell(portfolio, asset, 4, new BigDecimal("100.00")));
    }
}
