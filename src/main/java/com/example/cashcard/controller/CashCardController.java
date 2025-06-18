package com.example.cashcard.controller;

import com.example.cashcard.repository.CashCardRepository;
import com.example.cashcard.model.CashCard;
import com.example.cashcard.service.CashCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;
@RestController
@RequestMapping("/cashcards")
public class CashCardController {

    private final CashCardService cashCardService;

    @Autowired
    public CashCardController (CashCardService cashCardService){
        this.cashCardService = cashCardService;
    }
    @GetMapping("/{requestedId}")
    public ResponseEntity<CashCard> findById(@PathVariable Long requestedId) {
        Optional<CashCard> cashCardOptional = cashCardService.findById(requestedId);

        if (cashCardOptional.isPresent()) {
            return ResponseEntity.ok(cashCardOptional.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Void> createCashCard (@RequestBody CashCard newCashCard, UriComponentsBuilder ucb){
        CashCard cashCard = cashCardService.createCashCard(newCashCard);
        URI location = ucb.path("cashcards/{id}").buildAndExpand(cashCard.getId()).toUri();
        System.out.println(location);
        return ResponseEntity.created(location).build();
    }



}
