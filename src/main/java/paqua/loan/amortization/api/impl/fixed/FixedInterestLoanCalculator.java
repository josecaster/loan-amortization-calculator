package paqua.loan.amortization.api.impl.fixed;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import paqua.loan.amortization.api.LoanAmortizationCalculator;
import paqua.loan.amortization.api.impl.TaxResult;
import paqua.loan.amortization.dto.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FixedInterestLoanCalculator implements LoanAmortizationCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger(FixedInterestLoanCalculator.class);

    @Override
    public LoanAmortization calculate(Loan loan) {
        BigDecimal overPaidInterestAmount = BigDecimal.ZERO;
        Map<Integer, EarlyPayment> earlyPayments = loan.getEarlyPayments() != null ? loan.getEarlyPayments() : Collections.emptyMap();

        BigDecimal principal = loan.getAmount();
        BigDecimal totalInterest = principal.multiply(loan.getRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)).multiply(BigDecimal.valueOf(loan.getTerm()));
        BigDecimal totalDue = principal.add(totalInterest);

        BigDecimal monthlyInterest = principal.multiply(loan.getRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        int term = loan.getTerm();
        BigDecimal monthlyPrincipal = principal.divide(BigDecimal.valueOf(term), 2, RoundingMode.HALF_UP);
        LocalDate paymentDate = loan.getFirstPaymentDate();

        List<MonthlyPayment> payments = new ArrayList<>();
        BigDecimal remainingPrincipal = principal;
        BigDecimal paymentAmount = monthlyInterest.add(monthlyPrincipal);

        LoanAmortization.LoanAmortizationBuilder amortizationBuilder = LoanAmortization.builder()
                .monthlyPaymentAmount(paymentAmount);

        for (int i = 0; i < term; i++) {
            BigDecimal additionalPaymentAmount = BigDecimal.ZERO;
            EarlyPayment earlyPayment = earlyPayments.get(i);

            if (earlyPayment != null) {
                additionalPaymentAmount = earlyPayment.getAmount();
                remainingPrincipal = remainingPrincipal.subtract(additionalPaymentAmount).subtract(paymentAmount);

                totalDue = totalDue.subtract(monthlyInterest.add(monthlyPrincipal).add(additionalPaymentAmount));
                if(totalDue.compareTo(BigDecimal.ZERO) < 0){
                    throw new IllegalArgumentException("Too much money");
                }

                if (earlyPayment.getStrategy() == EarlyPaymentStrategy.DECREASE_MONTHLY_PAYMENT) {
                    if(paymentAmount.compareTo(BigDecimal.ZERO) <= 0){
                        continue;
                    }

                    TaxResult taxResult = TaxResult.calculateTax(loan, monthlyInterest, monthlyPrincipal);
                    payments.add(new MonthlyPayment(i, remainingPrincipal, taxResult.getUpdatedMonthlyPrincipal(), taxResult.getUpdatedMonthlyInterest(), paymentAmount.add(taxResult.getTaxAmount()), additionalPaymentAmount, paymentDate, taxResult.getVatAmount()));
                    monthlyPrincipal = remainingPrincipal.divide(BigDecimal.valueOf(term-i), 2, RoundingMode.HALF_UP);
                    paymentAmount = monthlyInterest.add(monthlyPrincipal);
                }
                if (earlyPayment.getStrategy() == EarlyPaymentStrategy.DECREASE_TERM) {
                    term -= 1;
                    paymentAmount = monthlyInterest.add(monthlyPrincipal);
                    if(paymentAmount.compareTo(BigDecimal.ZERO) <= 0){
                        continue;
                    }
                    TaxResult taxResult = TaxResult.calculateTax(loan, monthlyInterest, monthlyPrincipal);
                    payments.add(new MonthlyPayment(i, remainingPrincipal, taxResult.getUpdatedMonthlyPrincipal(), taxResult.getUpdatedMonthlyInterest(), paymentAmount.add(taxResult.getTaxAmount()), additionalPaymentAmount, paymentDate, taxResult.getVatAmount()));
                }
            } else {

                totalDue = totalDue.subtract(monthlyInterest.add(monthlyPrincipal).add(additionalPaymentAmount));
                if(totalDue.compareTo(BigDecimal.ZERO) < 0){
//                    throw new IllegalArgumentException("Too much money");
                    paymentAmount = paymentAmount.add(totalDue);
                    monthlyPrincipal = paymentAmount.subtract(monthlyInterest);
                }

                if(paymentAmount.compareTo(BigDecimal.ZERO) <= 0){
                    continue;
                }
                TaxResult taxResult = TaxResult.calculateTax(loan, monthlyInterest, monthlyPrincipal);
                payments.add(new MonthlyPayment(i, remainingPrincipal, taxResult.getUpdatedMonthlyPrincipal(), taxResult.getUpdatedMonthlyInterest(), paymentAmount.add(taxResult.getTaxAmount()), additionalPaymentAmount, paymentDate, taxResult.getVatAmount()));
            }


            remainingPrincipal = remainingPrincipal.subtract(monthlyPrincipal);
            paymentDate = paymentDate.plusMonths(1);
        }
//        overPaidInterestAmount = overPaidInterestAmount.add(monthlyInterest);

        LoanAmortization result = amortizationBuilder
                .monthlyPayments(Collections.unmodifiableList(payments))
                .overPaymentAmount(totalInterest)
                .earlyPayments(earlyPayments)
                .build();

        LOGGER.debug("Calculation result: {}", result);
        return result;
    }

    public static BigDecimal getTaxAmountIncluded(Loan loan, BigDecimal monthlyInterest) {
        BigDecimal percentage = loan.getTaxPercentage();
        BigDecimal vatRate = percentage.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        // Step 1: Calculate the base price before VAT
        BigDecimal divisor = BigDecimal.ONE.add(vatRate); // Divisor is 1.10
        BigDecimal basePrice = monthlyInterest.divide(divisor, 2, RoundingMode.HALF_UP); // Base price before VAT

        // Step 2: Calculate the VAT amount
        BigDecimal vatAmount1 = monthlyInterest.subtract(basePrice); // VAT is the difference
        return vatAmount1;
    }
}
