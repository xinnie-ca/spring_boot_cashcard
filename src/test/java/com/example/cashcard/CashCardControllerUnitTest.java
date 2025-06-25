package com.example.cashcard;

import com.example.cashcard.controller.CashCardController;
import com.example.cashcard.dto.CashCardBulkUpdateDTO;
import com.example.cashcard.dto.CashCardRequestDTO;
import com.example.cashcard.dto.CashCardResponseDTO;
import com.example.cashcard.dto.FilterParamDTO;
import com.example.cashcard.model.CashCard;
import com.example.cashcard.service.CashCardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CashCardControllerUnitTest {

    @Mock
    private CashCardService cashCardService;

    @Mock
    private Principal principal;

    @InjectMocks
    private CashCardController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(principal.getName()).thenReturn("sarah1");
    }

    @Test
    void testFindById_Found() {
        CashCard card = new CashCard(1L, 100.0, "sarah1");
        when(cashCardService.findByIdAndOwner(1L, "sarah1")).thenReturn(Optional.of(card));

        ResponseEntity<CashCardResponseDTO> response = controller.findById(1L, principal);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getAmount()).isEqualTo(100.0);
    }

    @Test
    void testFindById_NotFound() {
        when(cashCardService.findByIdAndOwner(1L, "sarah1")).thenReturn(Optional.empty());
        ResponseEntity<CashCardResponseDTO> response = controller.findById(1L, principal);
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void testCreateCashCard() {
        CashCardRequestDTO dto = new CashCardRequestDTO(150.0);
        CashCard savedCard = new CashCard(1L, 150.0, "sarah1");
        when(cashCardService.createCashCard(dto, "sarah1")).thenReturn(savedCard);

        UriComponentsBuilder ucb = UriComponentsBuilder.fromUriString("http://localhost/cashcards/1");
        ResponseEntity<Void> response = controller.createCashCard(dto, ucb, principal);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testFindAll() {
        CashCard card = new CashCard(1L, 100.0, "sarah1");
        Page<CashCard> page = new PageImpl<>(List.of(card));
        when(cashCardService.findByOwner(any(Pageable.class), eq("sarah1"))).thenReturn(page);

        ResponseEntity<Iterable<CashCardResponseDTO>> response = controller.findAll(PageRequest.of(0, 10), principal);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testPutCashCard_Success() {
        CashCardRequestDTO dto = new CashCardRequestDTO(100.0);
        when(cashCardService.updateCashCard(1L, dto, "sarah1")).thenReturn(true);

        ResponseEntity<Void> response = controller.putCashCard(1L, dto, principal);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void testPutCashCard_NotFound() {
        when(cashCardService.updateCashCard(1L, new CashCardRequestDTO(100.0), "sarah1")).thenReturn(false);
        ResponseEntity<Void> response = controller.putCashCard(1L, new CashCardRequestDTO(100.0), principal);
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void testBulkUpdate_Success() {
        List<CashCardBulkUpdateDTO> updates = List.of(new CashCardBulkUpdateDTO(1L, 100.0));
        ResponseEntity<Void> response = controller.putCashcardBulk(updates, principal);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testBulkUpdate_EmptyList() {
        ResponseEntity<Void> response = controller.putCashcardBulk(List.of(), principal);
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void testDeleteCashCard_Success() {
        when(cashCardService.deleteCashCard(1L, "sarah1")).thenReturn(true);
        ResponseEntity<Void> response = controller.deleteCashCard(1L, principal);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testDeleteCashCard_NotFound() {
        when(cashCardService.deleteCashCard(1L, "sarah1")).thenReturn(false);
        ResponseEntity<Void> response = controller.deleteCashCard(1L, principal);
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void testFilterRange_ValidParams() {
        FilterParamDTO dto = new FilterParamDTO(10.0, 100.0);
        when(cashCardService.findByAmountRange(10.0, 100.0, PageRequest.of(0, 10))).thenReturn(List.of());

        ResponseEntity<List<CashCardResponseDTO>> response = controller.getFilteredCashCards(dto, PageRequest.of(0, 10));
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void testFilterRange_BadParams() {
        FilterParamDTO dto = new FilterParamDTO(100.0, 10.0);
        ResponseEntity<List<CashCardResponseDTO>> response = controller.getFilteredCashCards(dto, PageRequest.of(0, 10));
        assertThat(response.getStatusCode()).isEqualTo(ResponseEntity.badRequest().build().getStatusCode());
    }
}