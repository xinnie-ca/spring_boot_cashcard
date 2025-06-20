package com.example.cashcard.controller;

import com.example.cashcard.model.CashCard;
import com.example.cashcard.repository.CashCardRepository;
import com.example.cashcard.service.CashCardService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import jakarta.validation.Valid;
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
    public ResponseEntity<Void> createCashCard (@Valid @RequestBody CashCard newCashCard, UriComponentsBuilder ucb, Principal principal){
        CashCard cashCard = cashCardService.createCashCard(newCashCard, principal.getName());
        URI location = ucb.path("cashcards/{id}").buildAndExpand(cashCard.getId()).toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public ResponseEntity<Iterable<CashCard>> findAll(Pageable pageable, Principal principal){
        Page<CashCard> page = cashCardService.findByOwner(pageable, principal.getName());
        return ResponseEntity.ok(page.getContent());

    }

    /*
    he update endpoint only update, it does not create a new record.
    If the updating cash card does not exist, the end point will return 404.
    If the log in client is not the owner of the card, the endpoint will return 404
    */
    @PutMapping("/{requestedId}")
    public ResponseEntity<Void> putCashCard(@PathVariable Long requestedId, @Valid @RequestBody CashCard cashCardUpdate, Principal principle){

       boolean success = cashCardService.updateCashCard(requestedId, cashCardUpdate,principle.getName());
       return success? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /*
    The delete endpoint:
    Returns 404 if the record does not exist or the log in client is not the
    owner of this cash card.
    Returns 204 no content is successful.
     */
    @DeleteMapping("/{requestedId}")
    public ResponseEntity<Void> deleteCashCard(@PathVariable Long requestedId, Principal principal){
        boolean success = cashCardService.deleteCashCard(requestedId,principal.getName());
        return success? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }



}
