package com.fabiano.tradeforge.entities;

import com.fabiano.tradeforge.enums.AssetType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_asset")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;

    private String name;

    private AssetType assetType;

    //Bolsa/Mercado em que aquele ativo foi negociado. -> B3 / // NASDAQ
    private String exchange;

    // Como se fosse a moeda -> //USD
    private String currency;

    private Boolean tradable;   // true

}
