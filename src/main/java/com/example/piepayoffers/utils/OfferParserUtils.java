package com.example.piepayoffers.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OfferParserUtils {
    public static ParsedOfferDetails parseSummary(String summary) {
        String discountType;
        boolean percentage;
        double discountValue = 0;
        double minAmount = 0;

        if (summary.contains("%")) {
            discountType = "PERCENTAGE";
            percentage = true;

            Pattern percentPattern = Pattern.compile("(\\d+(\\.\\d+)?)%");
            Matcher matcher = percentPattern.matcher(summary);
            if (matcher.find()) {
                discountValue = Double.parseDouble(matcher.group(1));
            }

        } else {
            discountType = "FLAT";
            percentage = false;

            Pattern flatPattern = Pattern.compile("₹\\s*([\\d,]+)");
            Matcher matcher = flatPattern.matcher(summary);
            if (matcher.find()) {
                discountValue = Double.parseDouble(
                        matcher.group(1).replace(",", "")
                );
            }
        }

        Pattern minPattern = Pattern.compile("Min[^₹]*₹\\s*([\\d,]+)");
        Matcher minMatcher = minPattern.matcher(summary);
        if (minMatcher.find()) {
            minAmount = Double.parseDouble(
                    minMatcher.group(1).replace(",", "")
            );
        }

        return new ParsedOfferDetails(discountType, discountValue, percentage, minAmount);
    }


    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ParsedOfferDetails {
        private String discountType;
        private double discountValue;
        private boolean percentage;
        private double minAmount;

    }
}
