ALTER TABLE tb_position ADD CONSTRAINT uk_position_portfolio_asset UNIQUE (portfolio_id, asset_id);
