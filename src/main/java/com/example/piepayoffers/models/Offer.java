package com.example.piepayoffers.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String offerId;  // adjustment_id

    private String adjustmentType; // INSTANT_DISCOUNT, CASHBACK_ON_CARD

    @Lob
    private String summary;

    private String discountType; // FLAT or PERCENTAGE

    private double discountValue;

    private boolean percentage;

    private double minAmount;

    @ElementCollection
    @CollectionTable(name = "offer_banks", joinColumns = @JoinColumn(name = "offer_id"))
    @Column(name = "bank")
    private List<String> banks;

    @ElementCollection
    @CollectionTable(name = "offer_payment_instruments", joinColumns = @JoinColumn(name = "offer_id"))
    @Column(name = "payment_instrument")
    private List<String> paymentInstruments;

    @ElementCollection
    @CollectionTable(name = "offer_emi_months", joinColumns = @JoinColumn(name = "offer_id"))
    @Column(name = "emi_month")
    private List<String> emiMonths;
}

