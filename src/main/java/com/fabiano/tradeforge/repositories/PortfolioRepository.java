package com.fabiano.tradeforge.repositories;

import com.fabiano.tradeforge.entities.Portfolio;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Portfolio p where p.id = :id")
    Optional<Portfolio> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Portfolio p where p.user.id = :userId")
    Optional<Portfolio> findByUserIdForUpdate(@Param("userId") Long userId);
}
