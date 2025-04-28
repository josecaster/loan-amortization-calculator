package paqua.loan.amortization.api.impl;

import paqua.loan.amortization.dto.LoanTaxType;

import java.beans.ConstructorProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * This class handles tax calculations for loan payments.
 * It calculates and tracks tax amounts for both interest and principal components
 * based on the tax configuration (tax type, tax percentage, and whether tax is included or excluded).
 */
public class TaxResult {
    /**
     * Original interest amount before tax adjustments
     */
    private final BigDecimal originalInterestAmount;

    /**
     * Original principal amount before tax adjustments
     */
    private final BigDecimal originalPrincipalAmount;

    /**
     * Interest amount after tax adjustments
     */
    private final BigDecimal adjustedInterestAmount;

    /**
     * Principal amount after tax adjustments
     */
    private BigDecimal adjustedPrincipalAmount;

    /**
     * Tax amount applied to the interest portion
     */
    private final BigDecimal interestTaxAmount;

    /**
     * Tax amount applied to the principal portion
     */
    private BigDecimal principalTaxAmount;

    /**
     * Whether tax was included in the original amounts (true) or excluded/added on top (false)
     */
    private final boolean taxIncluded;

    /**
     * Constructor for TaxResult.
     *
     * @param originalInterestAmount Original interest amount before tax adjustments
     * @param originalPrincipalAmount Original principal amount before tax adjustments
     * @param adjustedInterestAmount Interest amount after tax adjustments
     * @param adjustedPrincipalAmount Principal amount after tax adjustments
     * @param interestTaxAmount Tax amount applied to the interest portion
     * @param principalTaxAmount Tax amount applied to the principal portion
     * @param taxIncluded Whether tax was included in the original amounts
     */
    @ConstructorProperties({
            "originalInterestAmount", "originalPrincipalAmount",
            "adjustedInterestAmount", "adjustedPrincipalAmount",
            "interestTaxAmount", "principalTaxAmount", "taxIncluded"
    })
    public TaxResult(
            BigDecimal originalInterestAmount, BigDecimal originalPrincipalAmount,
            BigDecimal adjustedInterestAmount, BigDecimal adjustedPrincipalAmount,
            BigDecimal interestTaxAmount, BigDecimal principalTaxAmount,
            boolean taxIncluded) {
        this.originalInterestAmount = originalInterestAmount;
        this.originalPrincipalAmount = originalPrincipalAmount;
        this.adjustedInterestAmount = adjustedInterestAmount;
        this.adjustedPrincipalAmount = adjustedPrincipalAmount;
        this.interestTaxAmount = interestTaxAmount;
        this.principalTaxAmount = principalTaxAmount;
        this.taxIncluded = taxIncluded;
    }

    /**
     * Calculates tax for a monthly payment based on the tax configuration.
     *
     * This method handles different tax scenarios:
     * 1. Tax included in the payment amount (tax is part of the given amounts)
     * 2. Tax excluded from the payment amount (tax is additional to the given amounts)
     * 3. Tax applied to interest only, principal only, or both
     *
     * @param taxIncluded Whether tax is included in the payment amount (true) or excluded (false)
     * @param taxType The type of tax application (INTEREST_ONLY, PRINCIPAL_ONLY, or BOTH)
     * @param taxRate The tax percentage rate (e.g., 21.0 for 21%)
     * @param interestAmount The monthly interest amount before tax adjustments
     * @param principalAmount The monthly principal amount before tax adjustments
     * @return A TaxResult object containing the calculated tax amounts and adjusted payment components
     */
    public static TaxResult calculate(
            Boolean taxIncluded,
            LoanTaxType taxType,
            BigDecimal taxRate,
            BigDecimal interestAmount,
            BigDecimal principalAmount) {

        // Default values
        BigDecimal interestTax = BigDecimal.ZERO;
        BigDecimal principalTax = BigDecimal.ZERO;
        BigDecimal adjustedInterest = interestAmount;
        BigDecimal adjustedPrincipal = principalAmount;
        boolean isTaxIncluded = taxIncluded != null && taxIncluded;

        // Only calculate if we have valid tax configuration
        if (taxIncluded != null && taxType != null && taxRate != null) {
            // Calculate tax on interest if applicable
            if (taxType == LoanTaxType.INTREST_ONLY || taxType == LoanTaxType.BOTH) {
                if (isTaxIncluded) {
                    // Tax is included in the interest amount
                    interestTax = extractIncludedTax(taxRate, interestAmount);
                    adjustedInterest = interestAmount.subtract(interestTax);
                } else {
                    // Tax is additional to the interest amount
                    interestTax = calculateExcludedTax(taxRate, interestAmount);
                    // Interest amount remains unchanged when tax is excluded
                }
            }

            // Calculate tax on principal if applicable
            if (taxType == LoanTaxType.PRINCIPAL_ONLY || taxType == LoanTaxType.BOTH) {
                if (isTaxIncluded) {
                    // Tax is included in the principal amount
                    principalTax = extractIncludedTax(taxRate, principalAmount);
                    adjustedPrincipal = principalAmount.subtract(principalTax);
                } else {
                    // Tax is additional to the principal amount
                    principalTax = calculateExcludedTax(taxRate, principalAmount);
                    // Principal amount remains unchanged when tax is excluded
                }
            }
        }

        return new TaxResult(
                interestAmount, principalAmount,
                adjustedInterest, adjustedPrincipal,
                interestTax, principalTax,
                isTaxIncluded
        );
    }

    /**
     * Calculates tax on an additional payment (early payment).
     *
     * Since additional payments are applied to the principal, this method only
     * applies tax adjustments when the tax type is BOTH or PRINCIPAL_ONLY.
     *
     * @param taxIncluded Whether tax is included in the payment amount (true) or excluded (false)
     * @param taxType The type of tax application (INTEREST_ONLY, PRINCIPAL_ONLY, or BOTH)
     * @param taxRate The tax percentage rate (e.g., 21.0 for 21%)
     * @param additionalPayment The additional payment amount
     * @return The adjusted additional payment amount after tax considerations
     */
    public static BigDecimal calculateAdditionalPayment(
            Boolean taxIncluded,
            LoanTaxType taxType,
            BigDecimal taxRate,
            BigDecimal additionalPayment) {

        // Only adjust if we have valid tax configuration and it applies to principal
        if (taxIncluded != null && taxIncluded && taxType != null && taxRate != null &&
                (taxType == LoanTaxType.BOTH || taxType == LoanTaxType.PRINCIPAL_ONLY)) {

            // Calculate tax included in the additional payment
            BigDecimal taxAmount = extractIncludedTax(taxRate, additionalPayment);

            // Subtract tax to get the actual principal payment amount
            return additionalPayment.subtract(taxAmount);
        }

        // Return original amount if no adjustment needed
        return additionalPayment;
    }

    /**
     * Extracts the tax amount when tax is included in a given amount.
     *
     * This method uses the reverse tax calculation:
     * 1. Calculate the base amount (before tax) by dividing by (1 + tax rate)
     * 2. Calculate the tax amount by subtracting the base amount from the total
     *
     * @param taxRate The tax percentage rate (e.g., 21.0 for 21%)
     * @param amountWithTax The amount including tax
     * @return The tax portion of the given amount
     */
    public static BigDecimal extractIncludedTax(BigDecimal taxRate, BigDecimal amountWithTax) {
        // Convert percentage to decimal rate (e.g., 21% becomes 0.21)
        BigDecimal decimalRate = taxRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        // Calculate divisor (1 + tax rate)
        BigDecimal divisor = BigDecimal.ONE.add(decimalRate);

        // Calculate base amount before tax
        BigDecimal baseAmount = amountWithTax.divide(divisor, 2, RoundingMode.HALF_UP);

        // Calculate tax amount (total - base)
        return amountWithTax.subtract(baseAmount);
    }

    /**
     * Calculates the tax amount to be added to a given amount.
     *
     * @param taxRate The tax percentage rate (e.g., 21.0 for 21%)
     * @param baseAmount The base amount before tax
     * @return The tax amount to be added
     */
    public static BigDecimal calculateExcludedTax(BigDecimal taxRate, BigDecimal baseAmount) {
        // Convert percentage to decimal rate (e.g., 21% becomes 0.21)
        BigDecimal decimalRate = taxRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        // Calculate tax amount (base * rate)
        return baseAmount.multiply(decimalRate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * @return Total tax amount (interest tax + principal tax)
     */
    public BigDecimal getTotalTaxAmount() {
        return interestTaxAmount.add(principalTaxAmount);
    }

    /**
     * @return Original interest amount before tax adjustments
     */
    public BigDecimal getOriginalInterestAmount() {
        return originalInterestAmount;
    }

    /**
     * @return Original principal amount before tax adjustments
     */
    public BigDecimal getOriginalPrincipalAmount() {
        return originalPrincipalAmount;
    }

    /**
     * @return Interest amount after tax adjustments
     */
    public BigDecimal getAdjustedInterestAmount() {
        return adjustedInterestAmount;
    }

    /**
     * @return Principal amount after tax adjustments
     */
    public BigDecimal getAdjustedPrincipalAmount() {
        return adjustedPrincipalAmount;
    }

    /**
     * @return Tax amount applied to the interest portion
     */
    public BigDecimal getInterestTaxAmount() {
        return interestTaxAmount;
    }

    /**
     * @return Tax amount applied to the principal portion
     */
    public BigDecimal getPrincipalTaxAmount() {
        return principalTaxAmount;
    }

    /**
     * @return Whether tax was included in the original amounts
     */
    public boolean isTaxIncluded() {
        return taxIncluded;
    }

    /**
     * @return Total payment amount after tax adjustments
     */
    public BigDecimal getTotalAdjustedAmount() {
        return getTotalTaxAmount().compareTo(BigDecimal.ZERO) == 0? BigDecimal.ZERO : adjustedInterestAmount.add(adjustedPrincipalAmount);
    }

    /**
     * @return Total original amount before tax adjustments
     */
    public BigDecimal getTotalOriginalAmount() {
        return originalInterestAmount.add(originalPrincipalAmount);
    }

    public void setAdjustedPrincipalAmount(BigDecimal adjustedPrincipalAmount) {
        this.adjustedPrincipalAmount = adjustedPrincipalAmount;
    }

    public void setPrincipalTaxAmount(BigDecimal principalTaxAmount) {
        this.principalTaxAmount = principalTaxAmount;
    }

    /**
     * @return Total amount including tax (for tax-excluded scenario)
     */
    public BigDecimal getTotalAmountWithTax() {
        if (taxIncluded) {
            return getTotalOriginalAmount();
        } else {
            return getTotalAdjustedAmount().add(getTotalTaxAmount());
        }
    }

    /**
     * Prints a formatted log of the tax calculation results.
     * This method displays the tax amounts and adjusted payment components in a tabular format.
     */
    public void printLog() {
        System.out.println("\n=== TAX CALCULATION SUMMARY ===");

        // Print tax configuration
        System.out.println("\nTax Configuration:");
        System.out.println("Tax is " + (taxIncluded ? "INCLUDED in" : "EXCLUDED from") + " the payment amounts");

        // Print original and adjusted amounts
        System.out.println("\nPayment Components:");
        System.out.println("+----------------------+----------------------+----------------------+----------------------+");
        System.out.printf("| %-20s | %-20s | %-20s | %-20s |\n",
                "Component", "Original Amount", "Tax Amount", "Adjusted Amount");
        System.out.println("+----------------------+----------------------+----------------------+----------------------+");

        // Print interest row
        System.out.printf("| %-20s | %-20s | %-20s | %-20s |\n",
                "Interest", originalInterestAmount, interestTaxAmount, adjustedInterestAmount);
        System.out.println("+----------------------+----------------------+----------------------+----------------------+");

        // Print principal row
        System.out.printf("| %-20s | %-20s | %-20s | %-20s |\n",
                "Principal", originalPrincipalAmount, principalTaxAmount, adjustedPrincipalAmount);
        System.out.println("+----------------------+----------------------+----------------------+----------------------+");

        // Print total row
        System.out.printf("| %-20s | %-20s | %-20s | %-20s |\n",
                "TOTAL", getTotalOriginalAmount(), getTotalTaxAmount(), getTotalAdjustedAmount());
        System.out.println("+----------------------+----------------------+----------------------+----------------------+");

        // Print final payment amount
        System.out.println("\nFinal Payment Amount:");
        if (taxIncluded) {
            System.out.println("Original Amount (including tax): " + getTotalOriginalAmount());
            System.out.println("  - Tax Component: " + getTotalTaxAmount());
            System.out.println("  - Base Component: " + getTotalAdjustedAmount());
        } else {
            System.out.println("Base Amount: " + getTotalAdjustedAmount());
            System.out.println("Tax Amount: " + getTotalTaxAmount());
            System.out.println("Total Amount (including tax): " + getTotalAmountWithTax());
        }
    }
}