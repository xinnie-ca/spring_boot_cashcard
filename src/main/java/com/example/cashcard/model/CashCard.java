package com.example.cashcard.model;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Objects;


@Entity
public class CashCard{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Positive
    private Double amount;

    private String owner;

    public CashCard(){}

    public CashCard(Long id, Double amount,String owner){
        this.id = id;
        this.amount = amount;
        this.owner=owner;
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    //If we use the recrod class we don't need to override these two methods.
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CashCard)) return false;
        CashCard cashCard = (CashCard) obj;
        return Objects.equals(id, cashCard.id)
                && Objects.equals(amount, cashCard.amount);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id, amount);
    }
}

