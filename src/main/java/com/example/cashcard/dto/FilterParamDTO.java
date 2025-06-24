package com.example.cashcard.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class FilterParamDTO {
    @NotNull
    @Min(0)
    private Double min;

    @NotNull
    @Min(0)
    private Double max;

    public Double getMax() {
        return max;
    }

    public Double getMin() {
        return min;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public void setMin(Double min) {
        this.min = min;
    }
}
