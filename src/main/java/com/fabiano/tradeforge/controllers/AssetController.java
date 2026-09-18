package com.fabiano.tradeforge.controllers;

import com.fabiano.tradeforge.dtos.request.AssetRequestDTO;
import com.fabiano.tradeforge.dtos.response.AssetResponseDTO;
import com.fabiano.tradeforge.services.AssetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(value = "/asset")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AssetResponseDTO> createAsset(@Valid @RequestBody AssetRequestDTO assetRequestDTO) {
        AssetResponseDTO dto = assetService.createAsset(assetRequestDTO);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{symbol}")
                .buildAndExpand(dto.symbol()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @GetMapping(value = "/{symbol}")
    public ResponseEntity<AssetResponseDTO> getAssetBySymbol(@PathVariable String symbol) {
        AssetResponseDTO dto = assetService.getAssetBySymbol(symbol);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{symbol}")
    public ResponseEntity<AssetResponseDTO> updateAsset(@PathVariable String symbol,
                                                          @Valid @RequestBody AssetRequestDTO assetRequestDTO) {
        AssetResponseDTO dto = assetService.updateAsset(symbol, assetRequestDTO);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(value = "/{symbol}/activate")
    public ResponseEntity<AssetResponseDTO> activateAsset(@PathVariable String symbol) {
        AssetResponseDTO dto = assetService.activateAsset(symbol);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(value = "/{symbol}/deactivate")
    public ResponseEntity<AssetResponseDTO> deactivateAsset(@PathVariable String symbol) {
        AssetResponseDTO dto = assetService.deactivateAsset(symbol);
        return ResponseEntity.ok(dto);
    }

}
