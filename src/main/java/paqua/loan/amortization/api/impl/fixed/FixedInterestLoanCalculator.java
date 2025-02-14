package paqua.loan.amortization.api.impl.fixed;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import paqua.loan.amortization.api.LoanAmortizationCalculator;
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
                    payments.add(new MonthlyPayment(i, remainingPrincipal, monthlyPrincipal, monthlyInterest, paymentAmount, additionalPaymentAmount, paymentDate));
                    monthlyPrincipal = remainingPrincipal.divide(BigDecimal.valueOf(term-i), 2, RoundingMode.HALF_UP);
                    paymentAmount = monthlyInterest.add(monthlyPrincipal);
                }
                if (earlyPayment.getStrategy() == EarlyPaymentStrategy.DECREASE_TERM) {
                    term -= 1;
                    paymentAmount = monthlyInterest.add(monthlyPrincipal);
                    if(paymentAmount.compareTo(BigDecimal.ZERO) <= 0){
                        continue;
                    }
                    payments.add(new MonthlyPayment(i, remainingPrincipal, monthlyPrincipal, monthlyInterest, paymentAmount, additionalPaymentAmount, paymentDate));
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
                payments.add(new MonthlyPayment(i, remainingPrincipal, monthlyPrincipal, monthlyInterest, paymentAmount, additionalPaymentAmount, paymentDate));
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
}
