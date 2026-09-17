package com.fabiano.tradeforge.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_portfolio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private BigDecimal cashBalance;
    private Instant createdAt;

    @OneToMany(mappedBy = "portfolio")
    private List<Position> positions =  new ArrayList<>();

    public Portfolio(User user, BigDecimal cashBalance, Instant createdAt) {
        this.user = user;
        this.cashBalance = cashBalance;
        this.createdAt = createdAt;
    }
}
