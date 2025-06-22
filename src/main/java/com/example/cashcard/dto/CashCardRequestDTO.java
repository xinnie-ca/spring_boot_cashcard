package com.example.cashcard.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CashCardRequestDTO {
    @NotNull(message = "Amount cannot be null.")
    @Positive(message = "Amount must be greater than 0.")
    private Double amount;

    public CashCardRequestDTO(){}

    public CashCardRequestDTO (Double amount){
        this.amount = amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getAmount() {
        return amount;
    }
}
