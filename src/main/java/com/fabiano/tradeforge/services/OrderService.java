package com.fabiano.tradeforge.services;

import com.fabiano.tradeforge.dtos.response.OrderResponseDTO;
import com.fabiano.tradeforge.entities.Order;
import com.fabiano.tradeforge.entities.Portfolio;
import com.fabiano.tradeforge.repositories.OrderRepository;
import com.fabiano.tradeforge.services.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponseDTO save(Order order) {
        return toDTO(orderRepository.save(order));
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
