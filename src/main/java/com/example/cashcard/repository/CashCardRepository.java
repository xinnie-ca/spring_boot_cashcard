package com.example.cashcard.repository;
import com.example.cashcard.model.CashCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface CashCardRepository extends JpaRepository<CashCard, Long> {
    Optional<CashCard> findByIdAndOwner(Long Id, String owner);
    Page<CashCard> findByOwner(String owner, Pageable pageable);
    boolean existsByIdAndOwner(Long id, String owner);

    @Query("SELECT c FROM CashCard c WHERE c.amount BETWEEN :min AND :max")
    List<CashCard> findByAmountRange(Double min, Double max, Pageable pageable);
}
