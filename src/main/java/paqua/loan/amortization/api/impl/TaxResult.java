package paqua.loan.amortization.api.impl;

import paqua.loan.amortization.dto.Loan;
import paqua.loan.amortization.dto.LoanTaxType;

import java.math.BigDecimal;

import static paqua.loan.amortization.api.impl.fixed.FixedInterestLoanCalculator.getTaxAmountIncluded;

public class TaxResult {
    private final BigDecimal vatAmount;
    private final BigDecimal taxAmount;
    private final BigDecimal updatedMonthlyInterest;
    private final BigDecimal updatedMonthlyPrincipal;

    public TaxResult(BigDecimal vatAmount, BigDecimal taxAmount, BigDecimal updatedMonthlyInterest, BigDecimal updatedMonthlyPrincipal) {
        this.vatAmount = vatAmount;
        this.taxAmount = taxAmount;
        this.updatedMonthlyInterest = updatedMonthlyInterest;
        this.updatedMonthlyPrincipal = updatedMonthlyPrincipal;
    }

    public BigDecimal getVatAmount() { return vatAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getUpdatedMonthlyInterest() { return updatedMonthlyInterest; }
    public BigDecimal getUpdatedMonthlyPrincipal() { return updatedMonthlyPrincipal; }

    public static TaxResult calculateTax(Loan loan, BigDecimal monthlyInterest, BigDecimal monthlyPrincipal) {
        BigDecimal vatAmount = null;
        BigDecimal taxAmount = null;

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
                    vatAmount = vatAmount.add(monthlyPrincipal.multiply(BigDecimal.valueOf(loan.getTaxPercentage().doubleValue() / 100)));
                }
                taxAmount = vatAmount;
            }
        }

        if (vatAmount == null) vatAmount = BigDecimal.ZERO;
        if (taxAmount == null) taxAmount = BigDecimal.ZERO;

        return new TaxResult(vatAmount, taxAmount, monthlyInterest, monthlyPrincipal);
    }

}
