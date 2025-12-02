package org.example.parking.model;

/**
 * Represents customer loyalty tiers with associated discount percentages.
 */
public enum LoyaltyTier {
    NONE(0.0),
    SILVER(0.10),
    GOLD(0.20),
    PLATINUM(0.30);

    private final double discountPercentage;

    LoyaltyTier(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    /**
     * Applies the loyalty discount to a base amount.
     * @param baseAmount the amount before discount
     * @return the discounted amount
     */
    public double applyDiscount(double baseAmount) {
        return baseAmount * (1.0 - discountPercentage);
    }
}

