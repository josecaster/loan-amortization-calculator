package paqua.loan.amortization.dto;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import paqua.loan.amortization.api.impl.TaxResult;

/**
 * Represents detailed information about monthly payment in loan amortization
 *
 * @author Artyom Panfutov
 */
public final class MonthlyPayment implements Serializable {
    private static final long serialVersionUID = -8046672296117265073L;

    /**
     * Month number (starts with 0)
     */
    private final Integer monthNumber;

    /**
     * Amount of remaining debt (balance)
     */
    private final BigDecimal loanBalanceAmount;

    /**
     * Amount of debt in payment (principal piece in payment)
     */
    private final BigDecimal debtPaymentAmount;

    /**
     * Amount of interest in payment
     */
    private final BigDecimal interestPaymentAmount;

    /**
     * Payment amount
     */
    private final BigDecimal paymentAmount;

    /**
     * Total tax amount (interest tax + principal tax)
     */
    private final BigDecimal taxAmount;

    /**
     * Tax amount applied to the interest portion
     */
    private final BigDecimal interestTaxAmount;

    /**
     * Tax amount applied to the principal portion
     */
    private final BigDecimal principalTaxAmount;

    /**
     * Additional payment
     */
    private final BigDecimal additionalPaymentAmount;

    /**
     * Payment date (optional)
     */
    private final LocalDate paymentDate;

    /**
     * Product payment breakdowns
     */
    private final List<ItemPayment> itemPayments;

    @ConstructorProperties({
            "monthNumber", "loanBalanceAmount", "debtPaymentAmount", "interestPaymentAmount",
            "paymentAmount", "additionalPaymentAmount", "paymentDate", "taxAmount",
            "interestTaxAmount", "principalTaxAmount", "productPayments"
    })
    public MonthlyPayment(
            Integer monthNumber,
            BigDecimal loanBalanceAmount,
            BigDecimal debtPaymentAmount,
            BigDecimal interestPaymentAmount,
            BigDecimal paymentAmount,
            BigDecimal additionalPaymentAmount,
            LocalDate paymentDate,
            BigDecimal taxAmount,
            BigDecimal interestTaxAmount,
            BigDecimal principalTaxAmount, List<ItemPayment> itemPayments) {
        this.monthNumber = monthNumber;
        this.loanBalanceAmount = loanBalanceAmount;
        this.debtPaymentAmount = debtPaymentAmount;
        this.interestPaymentAmount = interestPaymentAmount;
        this.paymentAmount = paymentAmount;
        this.additionalPaymentAmount = additionalPaymentAmount;
        this.paymentDate = paymentDate;
        this.taxAmount = taxAmount;
        this.interestTaxAmount = interestTaxAmount;
        this.principalTaxAmount = principalTaxAmount;
        this.itemPayments = itemPayments;
    }

    /**
     * @return Month number (starts with 0)
     */
    public Integer getMonthNumber() {
        return monthNumber;
    }

    /**
     * @return Amount of remaining debt (loan balance)
     */
    public BigDecimal getLoanBalanceAmount() {
        return loanBalanceAmount;
    }

    /**
     * @return Amount of debt in payment (principal debt amount in payment)
     */
    public BigDecimal getDebtPaymentAmount() {
        return debtPaymentAmount;
    }

    /**
     * @return Amount of interest in payment
     */
    public BigDecimal getInterestPaymentAmount() {
        return interestPaymentAmount;
    }

    /**
     * @return Amount of payment
     */
    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    /**
     * @return Additional payment amount
     */
    public BigDecimal getAdditionalPaymentAmount() {
        return additionalPaymentAmount;
    }

    /**
     * @return Payment date
     */
    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    /**
     * @return Total tax amount (interest tax + principal tax)
     */
    public BigDecimal getTaxAmount() {
        return taxAmount;
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
     * @return Product payment breakdowns
     */
    public List<ItemPayment> getProductPayments() {
        return itemPayments != null ? Collections.unmodifiableList(itemPayments) : Collections.emptyList();
    }

    public static MonthlyPaymentBuilder builder() {
        return new MonthlyPaymentBuilder();
    }

    /**
     * Builder for MonthlyPayment
     */
    public static final class MonthlyPaymentBuilder {
        private Integer monthNumber;
        private BigDecimal loanBalanceAmount;
        private BigDecimal debtPaymentAmount;
        private BigDecimal interestPaymentAmount;
        private BigDecimal paymentAmount;
        private BigDecimal additionalPaymentAmount;
        private LocalDate paymentDate;
        private BigDecimal taxAmount;
        private BigDecimal interestTaxAmount;
        private BigDecimal principalTaxAmount;
        private List<ItemPayment> itemPayments;

        public MonthlyPaymentBuilder() {
        }

        /**
         * Sets a month number
         * @param monthNumber month number
         * @return monthly payment builder
         */
        public MonthlyPaymentBuilder monthNumber(Integer monthNumber) {
            this.monthNumber = monthNumber;
            return this;
        }

        /**
         * Sets a remaing loan balance
         * @param loanBalanceAmount amount of loan balance
         * @return monthly payment builder
         */
        public MonthlyPaymentBuilder loanBalanceAmount(BigDecimal loanBalanceAmount) {
            this.loanBalanceAmount = loanBalanceAmount;
            return this;
        }

        /**
         * Sets an amount of a remaining debt
         * @param debtPaymentAmount debt payment amount
         * @return monthly payment builder
         */
        public MonthlyPaymentBuilder debtPaymentAmount(BigDecimal debtPaymentAmount) {
            this.debtPaymentAmount = debtPaymentAmount;
            return this;
        }

        /**
         * Sets an amount of interest in a payment
         * @param interestPaymentAmount amount of interest in a payment
         * @return monthly payment builder
         */
        public MonthlyPaymentBuilder interestPaymentAmount(BigDecimal interestPaymentAmount) {
            this.interestPaymentAmount = interestPaymentAmount;
            return this;
        }

        /**
         * Sets a payment amount
         * @param paymentAmount amount of a payment
         * @return monthly payment builder
         */
        public MonthlyPaymentBuilder paymentAmount(BigDecimal paymentAmount) {
            this.paymentAmount = paymentAmount;
            return this;
        }

        /**
         * Sets additional payment amount
         * @param additionalPaymentAmount additional payment amount
         * @return monthly payment builder
         */
        public MonthlyPaymentBuilder additionalPaymentAmount(BigDecimal additionalPaymentAmount) {
            this.additionalPaymentAmount = additionalPaymentAmount;
            return this;
        }

        /**
         * Sets a payment date
         * @param paymentDate date of the payment
         * @return monthly payment builder
         */
        public MonthlyPaymentBuilder paymentDate(LocalDate paymentDate) {
            this.paymentDate = paymentDate;
            return this;
        }

        /**
         * Sets the total tax amount
         * @param taxAmount total tax amount
         * @return monthly payment builder
         */
        public MonthlyPaymentBuilder taxAmount(BigDecimal taxAmount) {
            this.taxAmount = taxAmount;
            return this;
        }

        /**
         * Sets the interest tax amount
         * @param interestTaxAmount tax amount applied to interest
         * @return monthly payment builder
         */
        public MonthlyPaymentBuilder interestTaxAmount(BigDecimal interestTaxAmount) {
            this.interestTaxAmount = interestTaxAmount;
            return this;
        }

        /**
         * Sets the principal tax amount
         * @param principalTaxAmount tax amount applied to principal
         * @return monthly payment builder
         */
        public MonthlyPaymentBuilder principalTaxAmount(BigDecimal principalTaxAmount) {
            this.principalTaxAmount = principalTaxAmount;
            return this;
        }

        /**
         * Applies tax result to this builder
         * @param taxResult the tax calculation result
         * @return monthly payment builder
         */
        public MonthlyPaymentBuilder withTaxResult(TaxResult taxResult) {
            this.taxAmount = taxResult.getTotalTaxAmount();
            this.interestTaxAmount = taxResult.getInterestTaxAmount();
            this.principalTaxAmount = taxResult.getPrincipalTaxAmount();

            // Update interest and principal amounts to use the adjusted values
            this.interestPaymentAmount = taxResult.getAdjustedInterestAmount();
            this.debtPaymentAmount = taxResult.getAdjustedPrincipalAmount();

            return this;
        }

        /**
         * Sets product payment breakdowns
         * @param itemPayments list of product payments
         * @return monthly payment builder
         */
        public MonthlyPaymentBuilder productPayments(List<ItemPayment> itemPayments) {
            this.itemPayments = itemPayments;
            return this;
        }

        /**
         * Builds an immutable monthly payment object
         * @return monthly payment
         */
        public MonthlyPayment build() {
            // Default values for tax fields if not set
            BigDecimal finalInterestTaxAmount = interestTaxAmount != null ? interestTaxAmount : BigDecimal.ZERO;
            BigDecimal finalPrincipalTaxAmount = principalTaxAmount != null ? principalTaxAmount : BigDecimal.ZERO;

            // If taxAmount is not set but component tax amounts are, calculate the total
            if (taxAmount == null && (interestTaxAmount != null || principalTaxAmount != null)) {
                taxAmount = finalInterestTaxAmount.add(finalPrincipalTaxAmount);
            } else if (taxAmount == null) {
                taxAmount = BigDecimal.ZERO;
            }

            return new MonthlyPayment(
                    monthNumber,
                    loanBalanceAmount,
                    debtPaymentAmount,
                    interestPaymentAmount,
                    paymentAmount,
                    additionalPaymentAmount,
                    paymentDate,
                    taxAmount,
                    finalInterestTaxAmount,
                    finalPrincipalTaxAmount,
                    itemPayments
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MonthlyPayment that = (MonthlyPayment) o;
        return Objects.equals(monthNumber, that.monthNumber) &&
                Objects.equals(loanBalanceAmount, that.loanBalanceAmount) &&
                Objects.equals(debtPaymentAmount, that.debtPaymentAmount) &&
                Objects.equals(interestPaymentAmount, that.interestPaymentAmount) &&
                Objects.equals(paymentAmount, that.paymentAmount) &&
                Objects.equals(additionalPaymentAmount, that.additionalPaymentAmount) &&
                Objects.equals(paymentDate, that.paymentDate) &&
                Objects.equals(taxAmount, that.taxAmount) &&
                Objects.equals(interestTaxAmount, that.interestTaxAmount) &&
                Objects.equals(principalTaxAmount, that.principalTaxAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monthNumber, loanBalanceAmount, debtPaymentAmount, interestPaymentAmount,
                paymentAmount, additionalPaymentAmount, paymentDate, taxAmount,
                interestTaxAmount, principalTaxAmount);
    }

    @Override
    public String toString() {
        return "MonthlyPayment{" +
                "monthNumber=" + monthNumber +
                ", loanBalanceAmount=" + loanBalanceAmount +
                ", debtPaymentAmount=" + debtPaymentAmount +
                ", interestPaymentAmount=" + interestPaymentAmount +
                ", paymentAmount=" + paymentAmount +
                ", additionalPaymentAmount=" + additionalPaymentAmount +
                ", paymentDate=" + paymentDate +
                ", taxAmount=" + taxAmount +
                ", interestTaxAmount=" + interestTaxAmount +
                ", principalTaxAmount=" + principalTaxAmount +
                '}';
    }
}