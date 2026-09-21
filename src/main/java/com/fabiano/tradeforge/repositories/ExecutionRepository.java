package com.fabiano.tradeforge.repositories;

import com.fabiano.tradeforge.entities.Execution;
import com.fabiano.tradeforge.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExecutionRepository extends JpaRepository<Execution, Long> {

    List<Execution> findByOrder(Order order);
}
