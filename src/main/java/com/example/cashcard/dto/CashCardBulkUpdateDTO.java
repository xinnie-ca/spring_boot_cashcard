package com.example.cashcard.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CashCardBulkUpdateDTO {
    @NotNull(message = "ID cannot be null")
    @Positive(message = "ID must be positive")
    private Long id;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    private Double amount;

    public CashCardBulkUpdateDTO(){}

    public CashCardBulkUpdateDTO(Long id, Double amount){
        this.id = id;
        this.amount = amount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
