package com.example.cashcard.model;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;


@Entity
public class CashCard{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double amount;
    public CashCard(){}

    public CashCard(Long id, Double amount){
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