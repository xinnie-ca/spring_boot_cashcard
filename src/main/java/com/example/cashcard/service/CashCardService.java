package com.example.cashcard.service;

import com.example.cashcard.model.CashCard;
import com.example.cashcard.repository.CashCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CashCardService {

    private CashCardRepository cashCardRepository;
    @Autowired
    public CashCardService (CashCardRepository cashCardRepository){
        this.cashCardRepository = cashCardRepository;
    }

    public Optional<CashCard> findById(Long Id){
        return cashCardRepository.findById(Id);
    }

    public CashCard createCashCard(CashCard newCashcard){
        return cashCardRepository.save(newCashcard);
    }

}
