package com.example.piepayoffers.service;

import com.example.piepayoffers.dto.OfferResponse;
import com.example.piepayoffers.models.Offer;
import com.example.piepayoffers.repo.OfferRepository;
import com.example.piepayoffers.utils.OfferParserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OfferService {
    private final OfferRepository offerRepository;

    public OfferResponse saveOffers(Map<String, Object> flipkartPayload) {

        if (flipkartPayload == null || flipkartPayload.isEmpty()) {
            throw new IllegalArgumentException("Payload is empty or missing.");
        }

        List<Map<String, Object>> offers = (List<Map<String, Object>>) flipkartPayload.get("offers");

        if (offers == null || offers.isEmpty()) {
            throw new IllegalArgumentException("'offers' list is missing or empty.");
        }

        int totalIdentified = offers.size();
        int totalNewCreated = 0;

        for (Map<String, Object> offerJson : offers) {
            String offerId = (String) offerJson.get("adjustment_id");

            if (offerId == null || offerId.isBlank()) {
                throw new IllegalArgumentException("One of the offers is missing an 'adjustment_id'.");
            }

            boolean exists = offerRepository.findByOfferId(offerId).isPresent();
            if (!exists) {
                String summary = (String) offerJson.get("summary");

                if (summary == null || summary.isBlank()) {
                    throw new IllegalArgumentException("Offer with ID " + offerId + " has no 'summary'.");
                }

                OfferParserUtils.ParsedOfferDetails parsed = OfferParserUtils.parseSummary(summary);

                Map<String, Object> contributors = (Map<String, Object>) offerJson.get("contributors");
                if (contributors == null) {
                    throw new IllegalArgumentException("Offer with ID " + offerId + " is missing 'contributors'.");
                }

                List<String> banks = (List<String>) contributors.get("banks");
                List<String> paymentInstruments = (List<String>) contributors.get("payment_instrument");
                List<String> emiMonths = (List<String>) contributors.get("emi_months");

                Offer offer = Offer.builder()
                        .offerId(offerId)
                        .adjustmentType((String) offerJson.get("adjustment_type"))
                        .summary(summary)
                        .banks(banks)
                        .paymentInstruments(paymentInstruments)
                        .emiMonths(emiMonths)
                        .discountType(parsed.getDiscountType())
                        .discountValue(parsed.getDiscountValue())
                        .percentage(parsed.isPercentage())
                        .minAmount(parsed.getMinAmount())
                        .build();

                offerRepository.save(offer);
                totalNewCreated++;
            }
        }

        return new OfferResponse(totalIdentified, totalNewCreated);
    }


    public double getHighestDiscount(double amountToPay, String bankName, String paymentInstrument) {

        if (amountToPay <= 0) {
            throw new IllegalArgumentException("'amountToPay' must be greater than zero.");
        }

        if (bankName == null || bankName.isBlank()) {
            throw new IllegalArgumentException("'bankName' is required.");
        }

        if (paymentInstrument == null || paymentInstrument.isBlank()) {
            throw new IllegalArgumentException("'paymentInstrument' is required.");
        }

        List<Offer> offers = offerRepository.findByBankNameAndPaymentInstrument(bankName, paymentInstrument);

        double highestDiscount = 0;

        for (Offer offer : offers) {

            if (amountToPay < offer.getMinAmount()) {
                continue; // skip if amount does not meet minimum condition
            }

            double discount = offer.isPercentage()
                    ? amountToPay * (offer.getDiscountValue() / 100.0)
                    : offer.getDiscountValue();

            if (discount > highestDiscount) {
                highestDiscount = discount;
            }
        }

        return highestDiscount;
    }

}
