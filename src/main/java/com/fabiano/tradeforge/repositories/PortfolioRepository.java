package com.fabiano.tradeforge.repositories;

import com.fabiano.tradeforge.entities.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

}
