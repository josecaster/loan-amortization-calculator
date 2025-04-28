package paqua.loan.amortization.dto;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents the payment allocation for a single product in a monthly payment
 */
public class ItemPayment implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String productId;
    private final String productName;
    private final BigDecimal originalAmount;
    private final BigDecimal principalAmount;
    private final BigDecimal remainingBalance;
    private final BigDecimal additionalPaymentAmount;
    private final BigDecimal tax;

    @ConstructorProperties({
            "productId", "productName", "debtPaymentAmount", "originalAmount",
            "principalAmount", "remainingBalance", "additionalPaymentAmount", "tax"
    })
    public ItemPayment(String productId, String productName, BigDecimal originalAmount,
                       BigDecimal principalAmount, BigDecimal remainingBalance,
                       BigDecimal additionalPaymentAmount, BigDecimal tax) {
        this.productId = productId;
        this.productName = productName;
        this.originalAmount = originalAmount;
        this.principalAmount = principalAmount;
        this.remainingBalance = remainingBalance;
        this.additionalPaymentAmount = additionalPaymentAmount;
        this.tax = tax;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public BigDecimal getAdditionalPaymentAmount() {
        return additionalPaymentAmount;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String productId;
        private String productName;
        private BigDecimal originalAmount;
        private BigDecimal principalAmount;
        private BigDecimal remainingBalance;
        private BigDecimal additionalPaymentAmount;
        private BigDecimal tax;

        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder productName(String productName) {
            this.productName = productName;
            return this;
        }

        public Builder originalAmount(BigDecimal originalAmount) {
            this.originalAmount = originalAmount;
            return this;
        }

        public Builder principalAmount(BigDecimal principalAmount) {
            this.principalAmount = principalAmount;
            return this;
        }

        public Builder remainingBalance(BigDecimal remainingBalance) {
            this.remainingBalance = remainingBalance;
            return this;
        }

        public Builder additionalPaymentAmount(BigDecimal additionalPaymentAmount) {
            this.additionalPaymentAmount = additionalPaymentAmount;
            return this;
        }

        public Builder tax(BigDecimal tax) {
            this.tax = tax;
            return this;
        }

        public ItemPayment build() {
            return new ItemPayment(
                    productId,
                    productName,
                    originalAmount,
                    principalAmount,
                    remainingBalance,
                    additionalPaymentAmount,
                    tax);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPayment that = (ItemPayment) o;
        return Objects.equals(productId, that.productId) &&
                Objects.equals(productName, that.productName) &&
                Objects.equals(originalAmount, that.originalAmount) &&
                Objects.equals(principalAmount, that.principalAmount) &&
                Objects.equals(remainingBalance, that.remainingBalance) &&
                Objects.equals(additionalPaymentAmount, that.additionalPaymentAmount) &&
                Objects.equals(tax, that.tax);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, productName, originalAmount, principalAmount,
                remainingBalance, additionalPaymentAmount, tax);
    }

    @Override
    public String toString() {
        return "ProductPayment{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", originalAmount=" + originalAmount +
                ", principalAmount=" + principalAmount +
                ", remainingBalance=" + remainingBalance +
                ", additionalPaymentAmount=" + additionalPaymentAmount +
                ", tax=" + tax +
                '}';
    }
}