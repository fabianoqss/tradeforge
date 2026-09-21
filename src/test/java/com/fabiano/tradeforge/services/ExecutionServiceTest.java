package com.fabiano.tradeforge.services;

import com.fabiano.tradeforge.dtos.response.ExecutionResponseDTO;
import com.fabiano.tradeforge.entities.Execution;
import com.fabiano.tradeforge.entities.Order;
import com.fabiano.tradeforge.repositories.ExecutionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionServiceTest {

    @Test
    void recordExecutionShouldSaveExecutionAndReturnPersistedDTO() {
        ExecutionRepository executionRepository = Mockito.mock(ExecutionRepository.class);
        ExecutionService executionService = new ExecutionService(executionRepository);
        Order order = new Order();
        order.setId(10L);
        BigDecimal price = new BigDecimal("123.45");
        Instant persistedAt = Instant.parse("2026-09-21T12:00:00Z");
        Execution saved = new Execution(1L, order, price, 4, persistedAt);
        Mockito.when(executionRepository.save(Mockito.any(Execution.class))).thenReturn(saved);

        Instant before = Instant.now();
        var dto = executionService.recordExecution(order, price, 4);
        Instant after = Instant.now();

        ArgumentCaptor<Execution> captor = ArgumentCaptor.forClass(Execution.class);
        Mockito.verify(executionRepository).save(captor.capture());
        Execution execution = captor.getValue();
        assertNull(execution.getId());
        assertSame(order, execution.getOrder());
        assertEquals(price, execution.getExecutedPrice());
        assertEquals(4, execution.getExecutedQuantity());
        assertNotNull(execution.getExecutedAt());
        assertFalse(execution.getExecutedAt().isBefore(before));
        assertFalse(execution.getExecutedAt().isAfter(after));
        assertEquals(new ExecutionResponseDTO(1L, 10L, price, 4, persistedAt), dto);
    }

    @Test
    void findByOrderShouldMapMultipleExecutionsToDTOs() {
        ExecutionRepository executionRepository = Mockito.mock(ExecutionRepository.class);
        ExecutionService executionService = new ExecutionService(executionRepository);
        Order order = new Order();
        order.setId(10L);
        Instant firstAt = Instant.parse("2026-09-21T12:00:00Z");
        Instant secondAt = firstAt.plusSeconds(1);
        BigDecimal firstPrice = new BigDecimal("123.45");
        BigDecimal secondPrice = new BigDecimal("124.50");
        Mockito.when(executionRepository.findByOrder(order)).thenReturn(List.of(
                new Execution(1L, order, firstPrice, 4, firstAt),
                new Execution(2L, order, secondPrice, 6, secondAt)
        ));

        var result = executionService.findByOrder(order);

        assertEquals(List.of(
                new ExecutionResponseDTO(1L, 10L, firstPrice, 4, firstAt),
                new ExecutionResponseDTO(2L, 10L, secondPrice, 6, secondAt)
        ), result);
        Mockito.verify(executionRepository).findByOrder(order);
        Mockito.verifyNoMoreInteractions(executionRepository);
    }

    @Test
    void findByOrderShouldReturnEmptyListWhenNoExecutionsExist() {
        ExecutionRepository executionRepository = Mockito.mock(ExecutionRepository.class);
        ExecutionService executionService = new ExecutionService(executionRepository);
        Order order = new Order();
        order.setId(10L);
        Mockito.when(executionRepository.findByOrder(order)).thenReturn(List.of());

        var result = executionService.findByOrder(order);

        assertTrue(result.isEmpty());
        Mockito.verify(executionRepository).findByOrder(order);
        Mockito.verifyNoMoreInteractions(executionRepository);
    }
}
