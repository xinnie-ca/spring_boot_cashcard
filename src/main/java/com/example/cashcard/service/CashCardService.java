package com.example.cashcard.service;

import com.example.cashcard.model.CashCard;
import com.example.cashcard.repository.CashCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CashCardService {

    private CashCardRepository cashCardRepository;
    @Autowired
    public CashCardService (CashCardRepository cashCardRepository){
        this.cashCardRepository = cashCardRepository;
    }

    public Optional<CashCard> findById(Long id){
        return cashCardRepository.findById(id);
    }

    public CashCard createCashCard(CashCard newCashcard){
        return cashCardRepository.save(newCashcard);
    }

    public Page<CashCard> findAll(Pageable pageable){
        return cashCardRepository.findAll(pageable);
    }

}
