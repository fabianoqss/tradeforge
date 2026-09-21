package com.fabiano.tradeforge.repositories;

import com.fabiano.tradeforge.entities.Order;
import com.fabiano.tradeforge.entities.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByPortfolio(Portfolio portfolio);
}
