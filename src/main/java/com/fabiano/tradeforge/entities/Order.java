package com.fabiano.tradeforge.entities;

import com.fabiano.tradeforge.enums.OrderSide;
import com.fabiano.tradeforge.enums.OrderStatus;
import com.fabiano.tradeforge.enums.OrderType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "tb_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    private OrderType type;

    private Integer quantity;

    private BigDecimal limitPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Instant createdAt;

    private Instant executedAt;
}
