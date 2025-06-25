package com.example.cashcard;

import com.example.cashcard.dto.CashCardBulkUpdateDTO;
import com.example.cashcard.dto.CashCardRequestDTO;
import com.example.cashcard.dto.CashCardResponseDTO;
import com.example.cashcard.model.CashCard;
import com.example.cashcard.repository.CashCardRepository;
import com.example.cashcard.service.CashCardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CashCardServiceUnitTest {

    @Mock
    private CashCardRepository cashCardRepository;

    @InjectMocks
    private CashCardService cashCardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateCashCard() {
        CashCardRequestDTO dto = new CashCardRequestDTO(100.0);
        CashCard savedCard = new CashCard(1L, 100.0, "sarah1");

        when(cashCardRepository.save(any(CashCard.class))).thenReturn(savedCard);

        CashCard result = cashCardService.createCashCard(dto, "sarah1");

        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(100.0);
        assertThat(result.getOwner()).isEqualTo("sarah1");
    }

    @Test
    void testFindByIdAndOwner() {
        CashCard card = new CashCard(1L, 200.0, "sarah1");
        when(cashCardRepository.findByIdAndOwner(1L, "sarah1")).thenReturn(Optional.of(card));

        Optional<CashCard> result = cashCardService.findByIdAndOwner(1L, "sarah1");

        assertThat(result).isPresent();
        assertThat(result.get().getAmount()).isEqualTo(200.0);
    }

    @Test
    void testUpdateCashCardSuccess() {
        CashCard card = new CashCard(1L, 150.0, "sarah1");
        when(cashCardRepository.findByIdAndOwner(1L, "sarah1")).thenReturn(Optional.of(card));

        boolean updated = cashCardService.updateCashCard(1L, new CashCardRequestDTO(200.0), "sarah1");

        assertThat(updated).isTrue();
        verify(cashCardRepository).save(any(CashCard.class));
    }

    @Test
    void testUpdateCashCardFailure() {
        when(cashCardRepository.findByIdAndOwner(1L, "sarah1")).thenReturn(Optional.empty());

        boolean updated = cashCardService.updateCashCard(1L, new CashCardRequestDTO(200.0), "sarah1");

        assertThat(updated).isFalse();
        verify(cashCardRepository, never()).save(any());
    }

    @Test
    void testBulkUpdateThrowsIfAnyCardMissing() {
        List<CashCardBulkUpdateDTO> dtos = List.of(new CashCardBulkUpdateDTO(1L, 100.0));
        when(cashCardRepository.findByIdAndOwner(1L, "sarah1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cashCardService.bulkUpdate(dtos, "sarah1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not owned");
    }

    @Test
    void testDeleteCashCardSuccess() {
        when(cashCardRepository.existsByIdAndOwner(1L, "sarah1")).thenReturn(true);

        boolean deleted = cashCardService.deleteCashCard(1L, "sarah1");

        assertThat(deleted).isTrue();
        verify(cashCardRepository).deleteById(1L);
    }

    @Test
    void testDeleteCashCardFailure() {
        when(cashCardRepository.existsByIdAndOwner(1L, "sarah1")).thenReturn(false);

        boolean deleted = cashCardService.deleteCashCard(1L, "sarah1");

        assertThat(deleted).isFalse();
        verify(cashCardRepository, never()).deleteById(anyLong());
    }

    @Test
    void testBulkDeleteThrowsIfAnyCardMissing() {
        List<Long> ids = List.of(1L, 2L);
        when(cashCardRepository.existsByIdAndOwner(1L, "sarah1")).thenReturn(true);
        when(cashCardRepository.existsByIdAndOwner(2L, "sarah1")).thenReturn(false);

        assertThatThrownBy(() -> cashCardService.bulkDeleteCashCard(ids, "sarah1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testFindByAmountRange() {
        List<CashCard> mockCards = List.of(
                new CashCard(1L, 50.0, "sarah1"),
                new CashCard(2L, 75.0, "sarah1")
        );

        when(cashCardRepository.findByAmountRange(eq(10.0), eq(100.0), any(Pageable.class)))
                .thenReturn(mockCards);

        List<CashCardResponseDTO> result = cashCardService.findByAmountRange(10.0, 100.0, PageRequest.of(0, 5));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAmount()).isEqualTo(50.0);
    }
}