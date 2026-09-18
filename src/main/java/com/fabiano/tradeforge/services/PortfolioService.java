package com.fabiano.tradeforge.services;

import com.fabiano.tradeforge.dtos.response.PortfolioResponseDTO;
import com.fabiano.tradeforge.dtos.response.PositionResponseDTO;
import com.fabiano.tradeforge.entities.Portfolio;
import com.fabiano.tradeforge.entities.Position;
import com.fabiano.tradeforge.entities.User;
import com.fabiano.tradeforge.repositories.PortfolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PortfolioService {

    private final UserService userService;

    public PortfolioService( UserService userService) {
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public PortfolioResponseDTO getPortifolio() {
        User user = userService.authenticated();
        Portfolio portfolio = user.getPortfolio();

        List<PositionResponseDTO> positions = portfolio.getPositions().stream()
                .map(this::toPositionDTO)
                .toList();

        return new PortfolioResponseDTO(portfolio.getCashBalance(), positions);
    }

    private PositionResponseDTO toPositionDTO(Position position) {
        return new PositionResponseDTO(
                position.getAsset().getSymbol(),
                position.getQuantity(),
                position.getAveragePrice(),
                position.getRealizedPnl()
        );
    }
}
