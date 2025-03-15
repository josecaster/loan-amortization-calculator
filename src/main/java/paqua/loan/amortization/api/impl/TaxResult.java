package paqua.loan.amortization.api.impl;

import paqua.loan.amortization.dto.Loan;
import paqua.loan.amortization.dto.LoanTaxType;

import java.math.BigDecimal;

import static paqua.loan.amortization.api.impl.fixed.FixedInterestLoanCalculator.getTaxAmountIncluded;

public class TaxResult {
    private final BigDecimal totalVatAmount;
    private final BigDecimal totalVatExcludedAmount;
    private final BigDecimal totalVatExcludedPrincipalAmount;
    private final BigDecimal updatedMonthlyInterest;
    private final BigDecimal updatedMonthlyPrincipal;

    public TaxResult(BigDecimal totalVatAmount, BigDecimal totalVatExcludedAmount, BigDecimal totalVatExcludedPrincipalAmount, BigDecimal updatedMonthlyInterest, BigDecimal updatedMonthlyPrincipal) {
        this.totalVatAmount = totalVatAmount;
        this.totalVatExcludedAmount = totalVatExcludedAmount;
        this.totalVatExcludedPrincipalAmount = totalVatExcludedPrincipalAmount;
        this.updatedMonthlyInterest = updatedMonthlyInterest;
        this.updatedMonthlyPrincipal = updatedMonthlyPrincipal;
    }

    public BigDecimal getTotalVatAmount() { return totalVatAmount; }
    public BigDecimal getTotalVatExcludedAmount() { return totalVatExcludedAmount; }
    public BigDecimal getUpdatedMonthlyInterest() { return updatedMonthlyInterest; }
    public BigDecimal getUpdatedMonthlyPrincipal() { return updatedMonthlyPrincipal; }

    public BigDecimal getTotalVatExcludedPrincipalAmount() {
        return totalVatExcludedPrincipalAmount;
    }

    public static TaxResult calculateTax(Loan loan, BigDecimal monthlyInterest, BigDecimal monthlyPrincipal) {
        BigDecimal vatAmount = null;
        BigDecimal taxAmount = null;
        BigDecimal taxExcludedPrincipalAmount = null;

        if (loan.getIncludeTax() != null) {
            if (loan.getIncludeTax()) {
                vatAmount = getTaxAmountIncluded(loan, monthlyInterest);
                monthlyInterest = monthlyInterest.subtract(vatAmount);
                if (loan.getLoanTaxType().compareTo(LoanTaxType.BOTH) == 0) {
                    BigDecimal taxAmountIncluded = getTaxAmountIncluded(loan, monthlyPrincipal);
                    vatAmount = vatAmount.add(taxAmountIncluded);
                    monthlyPrincipal = monthlyPrincipal.subtract(taxAmountIncluded);
                }
            } else {
                vatAmount = monthlyInterest.multiply(BigDecimal.valueOf(loan.getTaxPercentage().doubleValue() / 100));
                if (loan.getLoanTaxType().compareTo(LoanTaxType.BOTH) == 0) {
                    taxExcludedPrincipalAmount = monthlyPrincipal.multiply(BigDecimal.valueOf(loan.getTaxPercentage().doubleValue() / 100));
                    vatAmount = vatAmount.add(taxExcludedPrincipalAmount);
                }
                taxAmount = vatAmount;
            }
        }

        if (vatAmount == null) vatAmount = BigDecimal.ZERO;
        if (taxAmount == null) taxAmount = BigDecimal.ZERO;
        if (taxExcludedPrincipalAmount == null) taxExcludedPrincipalAmount = BigDecimal.ZERO;

        return new TaxResult(vatAmount, taxAmount, taxExcludedPrincipalAmount, monthlyInterest, monthlyPrincipal);
    }

    public static BigDecimal calculateAdditionalPayment(Loan loan, BigDecimal additionalPayment) {
        BigDecimal vatAmount = null;
        BigDecimal taxAmount = null;

        if (loan.getIncludeTax() != null) {
//            if (loan.getIncludeTax()) {
                vatAmount = BigDecimal.ZERO;
                if (loan.getLoanTaxType().compareTo(LoanTaxType.BOTH) == 0) {
                    BigDecimal taxAmountIncluded = getTaxAmountIncluded(loan, additionalPayment);
                    vatAmount = vatAmount.add(taxAmountIncluded);
                    additionalPayment = additionalPayment.subtract(taxAmountIncluded);
                }
//            } else {
//                vatAmount = monthlyInterest.multiply(BigDecimal.valueOf(loan.getTaxPercentage().doubleValue() / 100));
//                if (loan.getLoanTaxType().compareTo(LoanTaxType.BOTH) == 0) {
//                    vatAmount = vatAmount.add(additionalPayment.multiply(BigDecimal.valueOf(loan.getTaxPercentage().doubleValue() / 100)));
//                }
//                taxAmount = vatAmount;
//            }
        }

        return additionalPayment;
    }

    public static BigDecimal calculateBalance(Loan loan, BigDecimal balance) {
        BigDecimal vatAmount = null;
        BigDecimal taxAmount = null;

        if (loan.getIncludeTax() != null) {
//            if (loan.getIncludeTax()) {
            vatAmount = BigDecimal.ZERO;
            if (loan.getLoanTaxType().compareTo(LoanTaxType.BOTH) == 0) {
                BigDecimal taxAmountIncluded = getTaxAmountIncluded(loan, balance);
                vatAmount = vatAmount.add(taxAmountIncluded);
                balance = balance.subtract(taxAmountIncluded);
            }
//            } else {
//                vatAmount = monthlyInterest.multiply(BigDecimal.valueOf(loan.getTaxPercentage().doubleValue() / 100));
//                if (loan.getLoanTaxType().compareTo(LoanTaxType.BOTH) == 0) {
//                    vatAmount = vatAmount.add(additionalPayment.multiply(BigDecimal.valueOf(loan.getTaxPercentage().doubleValue() / 100)));
//                }
//                taxAmount = vatAmount;
//            }
        }

        return balance;
    }

}
