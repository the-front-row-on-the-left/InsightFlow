package com.insightflow.billing.domain;

public enum PricingModel {
    PER_TOKEN("per_token"),
    PER_REQUEST("per_request"),
    FIXED("fixed");

    private final String code;

    PricingModel(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static PricingModel fromCode(String code) {
        for (PricingModel pricingModel : values()) {
            if (pricingModel.code.equalsIgnoreCase(code)) {
                return pricingModel;
            }
        }
        throw new IllegalArgumentException("Unsupported pricing model: " + code);
    }
}
