package com.fabiano.tradeforge.services;

import com.fabiano.tradeforge.dtos.response.ExecutionResponseDTO;
import com.fabiano.tradeforge.entities.Execution;
import com.fabiano.tradeforge.entities.Order;
import com.fabiano.tradeforge.repositories.ExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class ExecutionService {

    private final ExecutionRepository executionRepository;

    public ExecutionService(ExecutionRepository executionRepository) {
        this.executionRepository = executionRepository;
    }

    @Transactional
    public ExecutionResponseDTO recordExecution(Order order, BigDecimal price, Integer quantity) {
        Execution execution = new Execution();
        execution.setOrder(order);
        execution.setExecutedPrice(price);
        execution.setExecutedQuantity(quantity);
        execution.setExecutedAt(Instant.now());

        return toDTO(executionRepository.save(execution));
    }

    @Transactional(readOnly = true)
    public List<ExecutionResponseDTO> findByOrder(Order order) {
        return executionRepository.findByOrder(order).stream()
                .map(this::toDTO)
                .toList();
    }

    private ExecutionResponseDTO toDTO(Execution execution) {
        return new ExecutionResponseDTO(
                execution.getId(),
                execution.getOrder().getId(),
                execution.getExecutedPrice(),
                execution.getExecutedQuantity(),
                execution.getExecutedAt()
        );
    }
}
