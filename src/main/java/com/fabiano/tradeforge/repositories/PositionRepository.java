package com.fabiano.tradeforge.repositories;

import com.fabiano.tradeforge.entities.Asset;
import com.fabiano.tradeforge.entities.Portfolio;
import com.fabiano.tradeforge.entities.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findByPortfolioAndAsset(Portfolio portfolio, Asset asset);
}
