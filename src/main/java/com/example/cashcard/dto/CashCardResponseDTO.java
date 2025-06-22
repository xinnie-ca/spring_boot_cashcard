package com.example.cashcard.dto;

public class CashCardResponseDTO {

    private Long id;
    private Double amount;

    public CashCardResponseDTO() {}

    public CashCardResponseDTO(Long id, Double amount) {
        this.id = id;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
