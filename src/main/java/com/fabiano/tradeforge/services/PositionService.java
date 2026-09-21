package com.fabiano.tradeforge.services;

import com.fabiano.tradeforge.dtos.response.PositionResponseDTO;
import com.fabiano.tradeforge.entities.Asset;
import com.fabiano.tradeforge.entities.Portfolio;
import com.fabiano.tradeforge.entities.Position;
import com.fabiano.tradeforge.repositories.PositionRepository;
import com.fabiano.tradeforge.services.exceptions.InsufficientPositionException;
import com.fabiano.tradeforge.services.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PositionService {

    private final PositionRepository positionRepository;

    public PositionService(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    @Transactional(readOnly = true)
    public PositionResponseDTO findByPortfolioAndAsset(Portfolio portfolio, Asset asset) {
        Position position = positionRepository.findByPortfolioAndAsset(portfolio, asset)
                .orElseThrow(() -> new ResourceNotFoundException("Position Not Found"));

        return toDTO(position);
    }

    @Transactional
    public PositionResponseDTO save(Position position) {
        return toDTO(positionRepository.save(position));
    }

    @Transactional
    public PositionResponseDTO applyBuy(Portfolio portfolio, Asset asset, Integer quantity, BigDecimal price) {
        Position position = positionRepository.findByPortfolioAndAsset(portfolio, asset)
                .orElseGet(() -> {
                    Position newPosition = new Position();
                    newPosition.setPortfolio(portfolio);
                    newPosition.setAsset(asset);
                    newPosition.setQuantity(0);
                    newPosition.setAveragePrice(BigDecimal.ZERO);
                    newPosition.setRealizedPnl(BigDecimal.ZERO);
                    return newPosition;
                });

        BigDecimal currentQuantity = BigDecimal.valueOf(position.getQuantity());
        BigDecimal boughtQuantity = BigDecimal.valueOf(quantity);
        BigDecimal totalCost = position.getAveragePrice().multiply(currentQuantity)
                .add(price.multiply(boughtQuantity));
        BigDecimal newQuantity = currentQuantity.add(boughtQuantity);

        position.setAveragePrice(totalCost.divide(newQuantity, 8, java.math.RoundingMode.HALF_UP));
        position.setQuantity(position.getQuantity() + quantity);

        return toDTO(positionRepository.save(position));
    }

    @Transactional
    public PositionResponseDTO applySell(Portfolio portfolio, Asset asset, Integer quantity, BigDecimal price) {
        Position position = positionRepository.findByPortfolioAndAsset(portfolio, asset)
                .orElseThrow(() -> new InsufficientPositionException("No position to sell"));

        if (position.getQuantity() < quantity) {
            throw new InsufficientPositionException("Insufficient position quantity to sell");
        }

        BigDecimal soldQuantity = BigDecimal.valueOf(quantity);
        BigDecimal realizedGain = price.subtract(position.getAveragePrice()).multiply(soldQuantity);

        position.setRealizedPnl(position.getRealizedPnl().add(realizedGain));
        position.setQuantity(position.getQuantity() - quantity);

        return toDTO(positionRepository.save(position));
    }

    private PositionResponseDTO toDTO(Position position) {
        return new PositionResponseDTO(
                position.getAsset().getSymbol(),
                position.getQuantity(),
                position.getAveragePrice(),
                position.getRealizedPnl()
        );
    }
}
