package com.example.cashcard.service;

import com.example.cashcard.model.CashCard;
import com.example.cashcard.repository.CashCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;

@Service
public class CashCardService {

    private final CashCardRepository cashCardRepository;
    @Autowired
    public CashCardService (CashCardRepository cashCardRepository){
        this.cashCardRepository = cashCardRepository;
    }

    public Optional<CashCard> findById(Long id){
        return cashCardRepository.findById(id);
    }

    public CashCard createCashCard(CashCard cashcard, Principal principal){
        CashCard newCashCard = new CashCard(null, cashcard.getAmount(),principal.getName());
        return cashCardRepository.save(newCashCard);
    }

    public Page<CashCard> findAll(Pageable pageable){
        return cashCardRepository.findAll(pageable);
    }

    public Optional<CashCard> findByIdAndOwner(Long id, String owner){
        Optional<CashCard> cashCard = cashCardRepository.findByIdAndOwner(id,owner);
        return cashCard;
    }

    public Page<CashCard> findByOwner(Pageable pageable,String owner){
        return cashCardRepository.findByOwner(owner, PageRequest
                .of(pageable.getPageNumber(), pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.DESC,"amount"))));
    }

    public boolean updateCashCard(Long id, CashCard updateCashCard, Principal principal){
        Optional<CashCard> cashCard = findByIdAndOwner(id, principal.getName());
        if (!cashCard.isPresent()){
            return false;
        }
        CashCard cashCardUpdated = new CashCard(cashCard.get().getId(), updateCashCard.getAmount(), principal.getName());
        cashCardRepository.save(cashCardUpdated);
        return true;
    }

    public boolean deleteCashCard(Long id, Principal principal){
        boolean exist = cashCardRepository.existsByIdAndOwner(id, principal.getName());
        if (exist) {
            cashCardRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
