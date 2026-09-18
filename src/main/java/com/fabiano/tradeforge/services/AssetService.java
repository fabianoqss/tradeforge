package com.fabiano.tradeforge.services;

import com.fabiano.tradeforge.dtos.request.AssetRequestDTO;
import com.fabiano.tradeforge.dtos.response.AssetResponseDTO;
import com.fabiano.tradeforge.entities.Asset;
import com.fabiano.tradeforge.repositories.AssetRepository;
import com.fabiano.tradeforge.services.exceptions.AssetAlreadyExistsException;
import com.fabiano.tradeforge.services.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssetService {

    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Transactional
    public AssetResponseDTO createAsset(AssetRequestDTO assetRequestDTO) {
        if(assetRepository.existsBySymbol(assetRequestDTO.symbol())){
            throw new AssetAlreadyExistsException("Asset already exists!");
        }
        Asset asset = new Asset();

        dtoToEntity(assetRequestDTO, asset);

        assetRepository.save(asset);

        return new AssetResponseDTO(
                asset.getSymbol(),
                asset.getName(),
                asset.getAssetType(),
                asset.getExchange(),
                asset.getCurrency(),
                asset.getTradable()
        );
    }

    @Transactional(readOnly = true)
    public AssetResponseDTO getAssetBySymbol(String symbol) {
        Asset asset = assetRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Asset Not Found"));

        return new AssetResponseDTO(
                asset.getSymbol(),
                asset.getName(),
                asset.getAssetType(),
                asset.getExchange(),
                asset.getCurrency(),
                asset.getTradable()
        );
    }

    @Transactional
    public AssetResponseDTO updateAsset(String symbol, AssetRequestDTO assetRequestDTO) {
        Asset asset = assetRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Asset Not Found"));

        dtoToEntity(assetRequestDTO, asset);

        assetRepository.save(asset);

        return new AssetResponseDTO(
                asset.getSymbol(),
                asset.getName(),
                asset.getAssetType(),
                asset.getExchange(),
                asset.getCurrency(),
                asset.getTradable()
        );
    }

    @Transactional
    public AssetResponseDTO activateAsset(String symbol) {
        return setTradable(symbol, true);
    }

    @Transactional
    public AssetResponseDTO deactivateAsset(String symbol) {
        return setTradable(symbol, false);
    }

    private AssetResponseDTO setTradable(String symbol, boolean tradable) {
        Asset asset = assetRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Asset Not Found"));

        asset.setTradable(tradable);

        assetRepository.save(asset);

        return new AssetResponseDTO(
                asset.getSymbol(),
                asset.getName(),
                asset.getAssetType(),
                asset.getExchange(),
                asset.getCurrency(),
                asset.getTradable()
        );
    }

    private void dtoToEntity(AssetRequestDTO assetRequestDTO, Asset asset){
       asset.setSymbol(assetRequestDTO.symbol());
       asset.setName(assetRequestDTO.name());
       asset.setAssetType(assetRequestDTO.assetType());
       asset.setExchange(assetRequestDTO.exchange());
       asset.setCurrency(assetRequestDTO.currency());
       asset.setTradable(assetRequestDTO.tradable());
    }

}
