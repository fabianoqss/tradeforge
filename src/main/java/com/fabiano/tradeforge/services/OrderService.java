package com.fabiano.tradeforge.services;

import com.fabiano.tradeforge.dtos.request.OrderRequestDTO;
import com.fabiano.tradeforge.dtos.response.OrderResponseDTO;
import com.fabiano.tradeforge.entities.Asset;
import com.fabiano.tradeforge.entities.Order;
import com.fabiano.tradeforge.entities.Portfolio;
import com.fabiano.tradeforge.entities.User;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;
    private final PortfolioRepository portfolioRepository;
    private final MarketDataService marketDataService;
    private final PositionService positionService;
    private final UserService userService;

    public OrderService(
            OrderRepository orderRepository,
            AssetRepository assetRepository,
            PortfolioRepository portfolioRepository,
            MarketDataService marketDataService,
            PositionService positionService,
            UserService userService
    ) {
        this.orderRepository = orderRepository;
        this.assetRepository = assetRepository;
        this.portfolioRepository = portfolioRepository;
        this.marketDataService = marketDataService;
        this.positionService = positionService;
        this.userService = userService;
    }

    @Transactional
    public OrderResponseDTO placeOrder(OrderRequestDTO dto) {
        User user = userService.authenticated();
        Portfolio portfolio = user.getPortfolio();

        Asset asset = assetRepository.findBySymbol(dto.assetSymbol())
                .orElseThrow(() -> new ResourceNotFoundException("Asset Not Found"));

        if (!Boolean.TRUE.equals(asset.getTradable())) {
            throw new AssetNotTradableException("Asset is not tradable");
        }

        Order order = new Order();
        order.setPortfolio(portfolio);
        order.setAsset(asset);
        order.setSide(dto.side());
        order.setType(dto.type());
        order.setQuantity(dto.quantity());
        order.setLimitPrice(dto.limitPrice());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(Instant.now());

        if (order.getType() == OrderType.MARKET) {
            execute(order, portfolio, asset);
        }

        order = orderRepository.save(order);

        return toDTO(order);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order Not Found"));

        return toDTO(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> findByPortfolio(Portfolio portfolio) {
        return orderRepository.findByPortfolio(portfolio).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getMyOrders() {
        Portfolio portfolio = userService.authenticated().getPortfolio();
        return findByPortfolio(portfolio);
    }

    private void execute(Order order, Portfolio portfolio, Asset asset) {
        Quote quote = marketDataService.getLatestQuote(asset.getSymbol())
                .orElseThrow(() -> new QuoteUnavailableException("Quote unavailable for " + asset.getSymbol()));

        BigDecimal orderValue = quote.price().multiply(BigDecimal.valueOf(order.getQuantity()));

        switch (order.getSide()) {
            case BUY -> {
                if (portfolio.getCashBalance().compareTo(orderValue) < 0) {
                    throw new InsufficientBalanceException("Insufficient balance to place this order");
                }
                portfolio.setCashBalance(portfolio.getCashBalance().subtract(orderValue));
                positionService.applyBuy(portfolio, asset, order.getQuantity(), quote.price());
            }
            case SELL -> {
                positionService.applySell(portfolio, asset, order.getQuantity(), quote.price());
                portfolio.setCashBalance(portfolio.getCashBalance().add(orderValue));
            }
        }

        portfolioRepository.save(portfolio);

        order.setStatus(OrderStatus.EXECUTED);
        order.setExecutedAt(Instant.now());
    }

    private OrderResponseDTO toDTO(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getAsset().getSymbol(),
                order.getSide(),
                order.getType(),
                order.getQuantity(),
                order.getLimitPrice(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getExecutedAt()
        );
    }
}
