package com.fabiano.tradeforge.controllers;

import com.fabiano.tradeforge.dtos.response.PortfolioResponseDTO;
import com.fabiano.tradeforge.services.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<PortfolioResponseDTO> getPortifolio(){
        PortfolioResponseDTO dto = portfolioService.getPortifolio();
        return ResponseEntity.ok(dto);
    }


}
