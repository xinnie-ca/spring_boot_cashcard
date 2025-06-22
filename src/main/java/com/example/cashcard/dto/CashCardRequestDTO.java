package com.example.cashcard.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CashCardRequestDTO {
    @NotNull
    @Positive
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
