package com.fabiano.tradeforge.services;

import com.fabiano.tradeforge.dtos.response.PositionResponseDTO;
import com.fabiano.tradeforge.entities.Asset;
import com.fabiano.tradeforge.entities.Portfolio;
import com.fabiano.tradeforge.entities.Position;
import com.fabiano.tradeforge.repositories.PositionRepository;
import com.fabiano.tradeforge.services.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private PositionResponseDTO toDTO(Position position) {
        return new PositionResponseDTO(
                position.getAsset().getSymbol(),
                position.getQuantity(),
                position.getAveragePrice(),
                position.getRealizedPnl()
        );
    }
}
