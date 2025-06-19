package com.example.cashcard.controller;

import com.example.cashcard.model.CashCard;
import com.example.cashcard.repository.CashCardRepository;
import com.example.cashcard.service.CashCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import org.springframework.data.domain.Pageable;

import java.net.URI;
import java.security.Principal;
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
    public ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
        Optional<CashCard> cashCardOptional = cashCardService.findByIdAndOwner(requestedId, principal.getName());

        if (cashCardOptional.isPresent()) {
            return ResponseEntity.ok(cashCardOptional.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Void> createCashCard (@RequestBody CashCard newCashCard, UriComponentsBuilder ucb, Principal principal){
        CashCard cashCard = cashCardService.createCashCard(newCashCard, principal);
        URI location = ucb.path("cashcards/{id}").buildAndExpand(cashCard.getId()).toUri();
        System.out.println(location);
        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public ResponseEntity<Iterable<CashCard>> findAll(Pageable pageable, Principal principal){
        Page<CashCard> page = cashCardService.findByOwner(pageable, principal.getName());
        return ResponseEntity.ok(page.getContent());

    }

    @PutMapping("/{requestedId}")
    public ResponseEntity<Void> putCashCard(@PathVariable Long requestedId, @RequestBody CashCard cashCardUpdate, Principal principle){

       boolean success = cashCardService.updateCashCard(requestedId, cashCardUpdate,principle);
       return success? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{requestedId}")
    public ResponseEntity<Void> deleteCashCard(@PathVariable Long requestedId, Principal principal){
        boolean success = cashCardService.deleteCashCard(requestedId,principal);
        return success? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }



}
