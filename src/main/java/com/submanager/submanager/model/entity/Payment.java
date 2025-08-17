package com.submanager.submanager.model.entity;

import com.submanager.submanager.model.enums.PaymentStatus;
import com.submanager.submanager.model.enums.BillingCycle;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments",
        indexes = {
                @Index(name = "idx_pay_account", columnList = "account_id"),
                @Index(name = "idx_pay_subscription", columnList = "subscription_id"),
                @Index(name = "idx_pay_paidAt", columnList = "paidAt")
        }
)
@Getter @Setter
public class Payment extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PaymentStatus status;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 8)
    private String currency;

    private LocalDateTime paidAt;     // momento del pago

    private LocalDate periodStart;    // inicio del periodo cubierto por el pago
    private LocalDate periodEnd;      // fin del periodo cubierto (inclusive)

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private BillingCycle billingCycle; // ciclo aplicado al pago

    @Column(length = 40)
    private String provider;          // ej: "mock"

    @Column(length = 80)
    private String providerRef;       // referencia externa (simulada)

    @Column(length = 300)
    private String message;           // texto libre (motivo de error, etc)
}
