package com.submanager.submanager.model.entity;

import com.submanager.submanager.model.enums.BillingCycle;
import com.submanager.submanager.model.enums.SubscriptionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "subscriptions",
        indexes = {
                @Index(name = "idx_sub_account", columnList = "account_id"),
                @Index(name = "idx_sub_nextRenewal", columnList = "nextRenewalDate")
        })
@Getter @Setter
public class Subscription extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String provider;

    @Column(length = 80)
    private String plan;

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotBlank
    @Column(nullable = false, length = 10)
    private String currency;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private BillingCycle billingCycle;

    private LocalDate nextRenewalDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 12, nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    private LocalDate lastActivityDate;

    @Column(length = 255)
    private String notes;
}
