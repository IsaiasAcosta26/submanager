package com.submanager.submanager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class MonthlySummaryResponseDTO {
    private BigDecimal total;
    private BigDecimal paid;
    private BigDecimal pending;
}
