package com.fabiano.tradeforge.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_position")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Position {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Provavelmente puxaremos esse preço médio por meio de APIS
    private Double averagePrice;

    private Integer quantity;

    private Double realizedPnl;

    @ManyToOne
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;
}
