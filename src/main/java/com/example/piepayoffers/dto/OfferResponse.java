package com.example.piepayoffers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OfferResponse {
    private int noOfOffersIdentified;
    private int noOfNewOffersCreated;
}
