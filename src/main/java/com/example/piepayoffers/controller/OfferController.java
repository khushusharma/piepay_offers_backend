package com.example.piepayoffers.controller;

import com.example.piepayoffers.dto.OfferResponse;
import com.example.piepayoffers.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class OfferController {
    private final OfferService offerService;

    @PostMapping("/offer")
    public ResponseEntity<OfferResponse> saveOffers(@RequestBody Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("Request body must not be empty.");
        }

        OfferResponse response = offerService.saveOffers(payload);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/highest-discount")
    public ResponseEntity<Map<String, Double>> getHighestDiscount(
            @RequestParam Double amountToPay,
            @RequestParam String bankName,
            @RequestParam String paymentInstrument) {

        if (amountToPay == null || amountToPay <= 0) {
            throw new IllegalArgumentException("Query param 'amountToPay' must be greater than zero.");
        }

        if (bankName == null || bankName.isBlank()) {
            throw new IllegalArgumentException("Query param 'bankName' must not be blank.");
        }

        if (paymentInstrument == null || paymentInstrument.isBlank()) {
            throw new IllegalArgumentException("Query param 'paymentInstrument' must not be blank.");
        }

        double highestDiscount = offerService.getHighestDiscount(amountToPay, bankName, paymentInstrument);

        return ResponseEntity.ok(Map.of("highestDiscountAmount", highestDiscount));
    }

}
