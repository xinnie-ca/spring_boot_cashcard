package com.example.cashcard.controller;

import com.example.cashcard.model.CashCard;
import com.example.cashcard.service.CashCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.security.Principal;
import java.util.Optional;


@RestController
@RequestMapping("/cashcards")
public class CashCardController {

    private final CashCardService cashCardService;
    private static final Logger log = LoggerFactory.getLogger(CashCardController.class);
    @Autowired
    public CashCardController (CashCardService cashCardService){
        this.cashCardService = cashCardService;
    }

    /**
     * Retrieve a single cash card.
     * @param requestedId
     * @param principal authenticated user.
     * @return Http 200 if success
     *         Http 404 if not found or authenticated user is not the owner of the card.
     */
    @GetMapping("/{requestedId}")
    public ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
        log.info("Method findById() starts.");

        Optional<CashCard> cashCardOptional = cashCardService.findByIdAndOwner(requestedId, principal.getName());
        log.info("Cashcard {} is requested.",requestedId);
        if (cashCardOptional.isPresent()) {
            log.info("Method findById() ends with success.");

            return ResponseEntity.ok(cashCardOptional.get());
        }
        log.info("Method findById() ends with unsuccessful.");
        return ResponseEntity.notFound().build();
    }

    /**
     * Create a cashcard, ID is automatically generated, owner is current authenticated user.
     * @param newCashCard Request body received in the http request
     * @param ucb Spring injected uro builder
     * @param principal Current authenticated user
     * @return Http 201 with location header
     */
    @PostMapping
    public ResponseEntity<Void> createCashCard (@Valid @RequestBody CashCard newCashCard, UriComponentsBuilder ucb, Principal principal){
        log.info("Method createCashCard() starts.");
        CashCard cashCard = cashCardService.createCashCard(newCashCard, principal.getName());
        URI location = ucb.path("cashcards/{id}").buildAndExpand(cashCard.getId()).toUri();
        log.info("Cashcard {} is created",cashCard.getId());
        log.info("Method createCashCard() ends.");
        return ResponseEntity.created(location).build();
    }

    /**
     * Retrieve a list of cashcard that current authenticated user owns
     * @param pageable URL parameter.
     * @param principal Current authenticated user
     * @return Http 200 - a list of cashcard in the response body, can be empty.
     */
    @GetMapping
    public ResponseEntity<Iterable<CashCard>> findAll(Pageable pageable, Principal principal){
        log.info("Method findAll() starts.");
        Page<CashCard> page = cashCardService.findByOwner(pageable, principal.getName());
        log.info("Cashcard list of {}", principal.getName());
        log.info("Method findAll() ends.");
        return ResponseEntity.ok(page.getContent());

    }


    /**
     * Update a cashcard.
     * @param requestedId Cashcard ID
     * @param cashCardUpdate Updating data of the cashcard
     * @param principle Current authenticated user
     * @return Http 204 Not Content if update success, no body is returned in the response.
     *         Http 404 Not Found if the requested cashcard is not exist, or current authenticated user is not the owner.
     */
    @PutMapping("/{requestedId}")
    public ResponseEntity<Void> putCashCard(@PathVariable Long requestedId, @Valid @RequestBody CashCard cashCardUpdate, Principal principle){
        log.info("Method outCashCard() starts.");

        boolean success = cashCardService.updateCashCard(requestedId, cashCardUpdate,principle.getName());
        if (success) {
            log.info("Cashcard {} is updated.", requestedId);
        } else {

            log.error("Cashcard {} is not updated", requestedId);
        }
        log.info("Method outCashCard() ends.");
        return success? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Delete a cashcard record - hard delete
     * @param requestedId Cashcard ID
     * @param principal Current authenticated user
     * @return Http 204 no Content if the deletion was successful.
     *         Http 404 not found if the cashcard does not exist or the current authenticated user is
     *              not the owner.
     */
    @DeleteMapping("/{requestedId}")
    public ResponseEntity<Void> deleteCashCard(@PathVariable Long requestedId, Principal principal){
        log.info("Method deleteCashCard() starts.");

        boolean success = cashCardService.deleteCashCard(requestedId,principal.getName());
        if (success) {
            log.info("Cashcard {} is deleted.", requestedId);
        } else {
            log.error("Cashcard {} is not deleted", requestedId);
        }
        log.info("Method deleteCashCard() ends.");
        return success? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

}
