package com.fabiano.tradeforge.repositories;

import com.fabiano.tradeforge.entities.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {

   boolean existsBySymbol(String symbol);

   Optional<Asset> findBySymbol(String symbol);
}
