package com.example.cashcard.controller;

import com.example.cashcard.dto.CashCardBulkUpdateDTO;
import com.example.cashcard.dto.CashCardRequestDTO;
import com.example.cashcard.dto.CashCardResponseDTO;
import com.example.cashcard.model.CashCard;
import com.example.cashcard.service.CashCardService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import java.util.List;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;


@RestController
@RequestMapping("/cashcards")
@SecurityRequirement(name = "basicAuth")
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
    @Operation(summary = "Get a CashCard by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found the cash card",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CashCardResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cash card not found",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<CashCardResponseDTO> findById(@PathVariable Long requestedId, Principal principal) {
        log.info("Method findById() starts.");

        Optional<CashCard> cashCardOptional = cashCardService.findByIdAndOwner(requestedId, principal.getName());
        log.info("Cashcard {} is requested.",requestedId);
        if (cashCardOptional.isPresent()) {
            CashCard cashCard = cashCardOptional.get();
            CashCardResponseDTO cashCardResponseDTO = new CashCardResponseDTO(cashCard.getId(),cashCard.getAmount());
            log.info("Method findById() ends with success.");
            return ResponseEntity.ok(cashCardResponseDTO);
        }
        log.info("Method findById() ends with unsuccessful.");
        return ResponseEntity.notFound().build();
    }

    /**
     * Create a cashcard, ID is automatically generated, owner is current authenticated user.
     * @param cashCardRequestDTO Request body received in the http request
     * @param ucb Spring injected uro builder
     * @param principal Current authenticated user
     * @return Http 201 with location header
     *         Http 400 invalid create data
     */
    @PostMapping
    @Operation(summary = "Create a CashCard")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "CashCard created"),
            @ApiResponse(responseCode = "400", description = "Invalid amount entered")
    })
    public ResponseEntity<Void> createCashCard (@Valid @RequestBody CashCardRequestDTO cashCardRequestDTO, UriComponentsBuilder ucb, Principal principal){
        log.info("Method createCashCard() starts.");
        CashCard cashCard = cashCardService.createCashCard(cashCardRequestDTO, principal.getName());
        URI location = ucb.path("cashcards/{id}").buildAndExpand(cashCard.getId()).toUri();
        log.info("Cashcard {} is created",cashCard.getId());
        log.info("Method createCashCard() ends.");
        return ResponseEntity.created(location).build();
    }

    /**
     * Retrieve a list of cashcard that current authenticated user owns
     * @param pageable URL parameter.
     * @param principal Current authenticated user
     * @return Http 200 - a list of cashcard in the response body, can be empty if the authenticated
     *          client has no cashcards.
     */
    @GetMapping
    @Operation(summary = "Get a list of CashCards")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CashCards found"),
    })
    public ResponseEntity<Iterable<CashCardResponseDTO>> findAll(Pageable pageable, Principal principal){
        log.info("Method findAll() starts.");
        Page<CashCard> page = cashCardService.findByOwner(pageable, principal.getName());
        List<CashCardResponseDTO> responseDTO = page.map(card -> new CashCardResponseDTO(card.getId(),card.getAmount())).getContent();
        log.info("Cashcard list of {}", principal.getName());
        log.info("Method findAll() ends.");
        return ResponseEntity.ok(responseDTO);

    }


    /**
     * Update a cashcard.
     * @param requestedId Cashcard ID
     * @param cashCardRequestDTO Updating data of the cashcard
     * @param principle Current authenticated user
     * @return Http 204 Not Content if update success, no body is returned in the response.
     *         Http 404 Not Found if the requested cashcard is not exist, or current authenticated user is not the owner.
     *         Http 400 Invalid update data.
     */
    @PutMapping("/{requestedId}")
    @Operation(summary = "Update an existing CashCard")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "CashCard updated successfully"),
            @ApiResponse(responseCode = "404", description = "CashCard not found or not owned"),
            @ApiResponse(responseCode = "400", description = "Invalid update data")
    })
    public ResponseEntity<Void> putCashCard(@PathVariable Long requestedId,
                                            @Valid @RequestBody CashCardRequestDTO cashCardRequestDTO, Principal principle){
        log.info("Method putCashCard() starts.");

        boolean success = cashCardService.updateCashCard(requestedId, cashCardRequestDTO ,principle.getName());
        if (success) {
            log.info("Cashcard {} is updated.", requestedId);
        } else {

            log.info("Cashcard {} is not updated", requestedId);
        }
        log.info("Method putCashCard() ends.");
        return success? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Bulk update the cashcards
     * @param cashCardBulkUpdateDTOS
     * @param principal
     * @return
     */
    @PutMapping("/bulk")
    @Operation(summary="Bulk update cashcards")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "CashCards update successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid update data")
    })
    public ResponseEntity<Void> putCashcardBulk(
            @Valid @RequestBody List<@Valid CashCardBulkUpdateDTO> cashCardBulkUpdateDTOS, Principal principal){
        log.info("Receive payload: {}" , cashCardBulkUpdateDTOS);
        try {
            cashCardService.bulkUpdate(cashCardBulkUpdateDTOS, principal.getName());
        }catch (Exception e){
            log.error("BULK"+e.getMessage(), e);
        }
        return ResponseEntity.noContent().build();
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
    @Operation(summary = "Delete a CashCard by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "CashCard deleted successfully"),
            @ApiResponse(responseCode = "404", description = "CashCard not found or not owned")
    })
    public ResponseEntity<Void> deleteCashCard(@PathVariable Long requestedId, Principal principal){
        log.info("Method deleteCashCard() starts.");

        boolean success = cashCardService.deleteCashCard(requestedId,principal.getName());
        if (success) {
            log.info("Cashcard {} is deleted.", requestedId);
        } else {
            log.info("Cashcard {} is not deleted", requestedId);
        }
        log.info("Method deleteCashCard() ends.");
        return success? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }


}
