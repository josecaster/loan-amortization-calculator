/*
 * MIT License
 *
 * Copyright (c) 2021 Artyom Panfutov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package paqua.loan.amortization.api.impl.annual;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import paqua.loan.amortization.api.LoanAmortizationCalculator;
import paqua.loan.amortization.api.impl.TaxResult;
import paqua.loan.amortization.api.impl.repeating.EarlyPaymentRepeatingStrategy;
import paqua.loan.amortization.dto.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.*;

/**
 * Implementation of the annual payment loan amortization calculator
 *
 * @author Artyom Panfutov
 */
class AnnualPaymentLoanCalculator implements LoanAmortizationCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnualPaymentLoanCalculator.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public LoanAmortization calculate(Loan loan) {
        BigDecimal overPaidInterestAmount = BigDecimal.ZERO;

        // Validate that product amounts sum up to the loan amount if products are defined
        validateProductAmounts(loan);

        final Map<Integer, EarlyPayment> earlyPayments = loan.getEarlyPayments() != null ? loan.getEarlyPayments() : Collections.emptyMap();
        BigDecimal loanBalance = loan.getAmount();

        // Track remaining balance for each product
        Map<String, BigDecimal> productBalances = new HashMap<>();
        if(loan.getProducts() != null) {
            for (Item item : loan.getProducts()) {
                productBalances.put(item.getId(), item.getAmount());
            }
        }

        final int term = loan.getTerm();

        final BigDecimal monthlyInterestRate = getMonthlyInterestRate(loan.getRate());
        BigDecimal monthlyPaymentAmount = getMonthlyPaymentAmount(loanBalance, monthlyInterestRate, term);

        LoanAmortization.LoanAmortizationBuilder amortizationBuilder = LoanAmortization.builder()
                .monthlyPaymentAmount(monthlyPaymentAmount);

        LocalDate paymentDate = loan.getFirstPaymentDate();

        // Calculate amortization schedule
        List<MonthlyPayment> payments = new ArrayList<>();
        for (int i = 0; i < term; i++) {
            BigDecimal principalAmount;
            BigDecimal paymentAmount;
            BigDecimal additionalPaymentAmount = BigDecimal.ZERO;

            final BigDecimal interestAmount = calculateInterestAmount(loan, loanBalance, monthlyInterestRate, paymentDate);

            // If something gets negative for some reason (because of early payments) we stop calculating and correct the amount in the last payment
            if (interestAmount.compareTo(BigDecimal.ZERO) < 0 || loanBalance.compareTo(BigDecimal.ZERO) < 0) {
                final int lastPaymentNumber = i - 1;

                if (lastPaymentNumber >= 0) {
                    final MonthlyPayment lastPayment = payments.get(lastPaymentNumber);

                    BigDecimal interestPaymentAmount = lastPayment.getInterestPaymentAmount();
                    BigDecimal loanBalanceAmount = lastPayment.getLoanBalanceAmount();
                    TaxResult taxResult = TaxResult.calculate(loan.getTaxDeductible(), loan.getLoanTaxType(), loan.getTaxPercentage(), interestPaymentAmount, loanBalanceAmount);

                    // Calculate product payments if products are defined
                    List<ItemPayment> itemPayments = calculateProductPayments(
                            loan.getProducts(),
                            productBalances,
                            lastPayment.getLoanBalanceAmount(),
                            additionalPaymentAmount
                    );

                    // Update product balances
                    for (ItemPayment itemPayment : itemPayments) {
                        BigDecimal currentBalance = productBalances.get(itemPayment.getProductId());
                        if (currentBalance != null) {
                            BigDecimal newBalance = currentBalance
                                    .subtract(itemPayment.getPrincipalAmount())
                                    .subtract(itemPayment.getAdditionalPaymentAmount());
                            productBalances.put(itemPayment.getProductId(), newBalance);
                        }
                    }

                    if(!itemPayments.isEmpty()){
                        BigDecimal productsTax = itemPayments.stream().map(ItemPayment::getTax).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
                        taxResult.setPrincipalTaxAmount(productsTax);
                        taxResult.setAdjustedPrincipalAmount(taxResult.getOriginalPrincipalAmount().subtract(productsTax));
                    }

                    // Create a new payment with corrected values
                    payments.set(lastPaymentNumber, MonthlyPayment.builder()
                            .monthNumber(lastPayment.getMonthNumber())
                            .additionalPaymentAmount(lastPayment.getAdditionalPaymentAmount())
                            // Use withTaxResult to set all tax-related fields
                            .withTaxResult(taxResult)
                            // Calculate the total payment amount (principal + interest + tax if excluded)
                            .paymentAmount(calculateCorrectedPaymentAmount(taxResult))
                            // Set the payment date
                            .paymentDate(lastPayment.getPaymentDate())
                            // Keep the original loan balance amount
                            .loanBalanceAmount(lastPayment.getLoanBalanceAmount())
                            .productPayments(itemPayments)
                            .build());
                }

                break;
            }

            // Check for early payment at this month
            EarlyPayment earlyPayment = getEarlyPaymentForMonth(earlyPayments, i);
            if (earlyPayment != null) {
                additionalPaymentAmount = earlyPayment.getAmount();
                additionalPaymentAmount = TaxResult.calculateAdditionalPayment(loan.getTaxDeductible(), loan.getLoanTaxType(), loan.getTaxPercentage(), additionalPaymentAmount);
            }

            if (i + 1 == loan.getTerm()) {
                principalAmount = loanBalance;
            } else {
                principalAmount = (monthlyPaymentAmount.subtract(interestAmount))
                        .add(additionalPaymentAmount)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            paymentAmount = interestAmount.add(principalAmount);

            BigDecimal interestPaymentAmount = interestAmount;
            BigDecimal debtPaymentAmount = principalAmount;
            TaxResult taxResult = TaxResult.calculate(loan.getTaxDeductible(), loan.getLoanTaxType(), loan.getTaxPercentage(), interestPaymentAmount, debtPaymentAmount);


            // Calculate product payments if products are defined
            List<ItemPayment> itemPayments = calculateProductPayments(
                    loan.getProducts(),
                    productBalances,
                    debtPaymentAmount,
                    additionalPaymentAmount
            );

            // Update product balances
            for (ItemPayment itemPayment : itemPayments) {
                BigDecimal currentBalance = productBalances.get(itemPayment.getProductId());
                if (currentBalance != null) {
                    BigDecimal newBalance = currentBalance
                            .subtract(itemPayment.getPrincipalAmount())
                            .subtract(itemPayment.getAdditionalPaymentAmount());
                    productBalances.put(itemPayment.getProductId(), newBalance);
                }
            }

            if(!itemPayments.isEmpty()){
                BigDecimal productsTax = itemPayments.stream().map(ItemPayment::getTax).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
                taxResult.setPrincipalTaxAmount(productsTax);
                taxResult.setAdjustedPrincipalAmount(taxResult.getOriginalPrincipalAmount().subtract(productsTax));
            }

            // Create the MonthlyPayment using the withTaxResult helper method
            MonthlyPayment payment = MonthlyPayment.builder()
                    .monthNumber(i)
                    // The withTaxResult method will set:
                    // - interestPaymentAmount to the adjusted interest
                    // - debtPaymentAmount to the adjusted principal
                    // - taxAmount to the total tax amount
                    // - interestTaxAmount and principalTaxAmount to their respective values
                    .withTaxResult(taxResult)
                    // Calculate payment amount - this is the total payment including tax
                    .paymentAmount(calculatePaymentAmount(taxResult))
                    // Set loan balance amount to the current loan balance BEFORE this payment
                    .loanBalanceAmount(loanBalance)
                    // Additional payment amount (unchanged)
                    .additionalPaymentAmount(additionalPaymentAmount)
                    // Payment date (unchanged)
                    .paymentDate(paymentDate)
                    .productPayments(itemPayments)
                    .build();
            payments.add(payment);

            // Update overPaidInterestAmount with the original interest amount (not adjusted)
            // This matches the expected behavior in the test case
            overPaidInterestAmount = overPaidInterestAmount.add(interestAmount);

            // For tax-included scenarios, we should only subtract the debtPaymentAmount
            // (which is already adjusted for tax) from the loan balance
            loanBalance = loanBalance.subtract(debtPaymentAmount);

            if (earlyPayment != null && earlyPayment.getStrategy() == EarlyPaymentStrategy.DECREASE_MONTHLY_PAYMENT) {
                BigDecimal additionalPaymentsWithRemainingLoanBalance = getTotalAmountOfEarlyPaymentsWithLoanBalanceUntilPayment(loan, loanBalance, i);

                if (term - 1 - i > 0) {
                    monthlyPaymentAmount = getMonthlyPaymentAmount(additionalPaymentsWithRemainingLoanBalance, monthlyInterestRate, term - 1 - i);
                }
            }

            if (loan.getFirstPaymentDate() != null && paymentDate != null) {
                paymentDate = getNextMonthPaymentDate(loan.getFirstPaymentDate(), paymentDate);
            }
        }

        LoanAmortization result = amortizationBuilder
                .monthlyPayments(Collections.unmodifiableList(payments))
                .overPaymentAmount(overPaidInterestAmount)
                .earlyPayments(earlyPayments)
                .build();

        LOGGER.debug("Calculation result: {}", result);

        return result;
    }

    /**
     * Validates that the sum of product amounts equals the loan amount
     */
    private void validateProductAmounts(Loan loan) {
        List<Item> items = loan.getProducts();
        if (items == null || items.isEmpty()) {
            LOGGER.debug("No products defined for loan, skipping validation");
            return;
        }

        BigDecimal productSum = BigDecimal.ZERO;
        for (Item item : items) {
            productSum = productSum.add(item.getAmount());
        }

        BigDecimal abs = productSum.subtract(loan.getAmount()).abs();
        if (abs.compareTo(new BigDecimal("0.00")) > 0) {
            LOGGER.warn("Sum of product amounts ({}) does not match loan amount ({})", productSum, loan.getAmount());
            throw new IllegalArgumentException(
                    String.format("Sum of product amounts (%s) does not match loan amount (%s)",
                            productSum, loan.getAmount())
            );
        }
    }

    /**
     * Calculates payment allocation for each product
     */
    private List<ItemPayment> calculateProductPayments(
            List<Item> items,
            Map<String, BigDecimal> productBalances,
            BigDecimal principalAmount,
            BigDecimal additionalPaymentAmount) {

        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        BigDecimal totalBalance = BigDecimal.ZERO;
        for (BigDecimal balance : productBalances.values()) {
            totalBalance = totalBalance.add(balance);
        }

        // If total balance is zero, return empty list
        if (totalBalance.compareTo(BigDecimal.ZERO) <= 0) {
            return Collections.emptyList();
        }

        List<ItemPayment> itemPayments = new ArrayList<>();

        for (Item item : items) {
            BigDecimal balance = productBalances.get(item.getId());
            if (balance == null || balance.compareTo(BigDecimal.ZERO) <= 0) {
                // Skip products that are already paid off
                itemPayments.add(ItemPayment.builder()
                        .productId(item.getId())
                        .productName(item.getName())
                        .originalAmount(item.getAmount())
                        .principalAmount(BigDecimal.ZERO)
                        .remainingBalance(BigDecimal.ZERO)
                        .additionalPaymentAmount(BigDecimal.ZERO)
                        .build());
                continue;
            }

            // Calculate proportion of this product in the total balance
            BigDecimal proportion = balance.divide(totalBalance, 15, RoundingMode.HALF_UP);

            // Allocate payment amounts based on proportion
            BigDecimal productPrincipal = principalAmount.multiply(proportion).setScale(2, RoundingMode.HALF_UP);
            BigDecimal productAdditional = additionalPaymentAmount.multiply(proportion).setScale(2, RoundingMode.HALF_UP);
            BigDecimal tax = BigDecimal.ZERO;
            if(item.getTax() != null){
                tax = productPrincipal.add(productAdditional).multiply(item.getTax().multiply(BigDecimal.valueOf(0.01)));
            }

            itemPayments.add(ItemPayment.builder()
                    .productId(item.getId())
                    .productName(item.getName())
                    .originalAmount(item.getAmount())
                    .principalAmount(productPrincipal)
                    .remainingBalance(balance)
                    .additionalPaymentAmount(productAdditional)
                    .tax(tax)
                    .build());
        }

        return itemPayments;
    }


    /**
     * Gets the early payment for the specified month, taking into account repeating strategies
     *
     * @param earlyPayments Map of early payments
     * @param monthNumber   Current month number
     * @return Early payment for the month, or null if none
     */
    private EarlyPayment getEarlyPaymentForMonth(Map<Integer, EarlyPayment> earlyPayments, int monthNumber) {
        // First, check if there's a direct early payment for this month
        EarlyPayment directPayment = earlyPayments.get(monthNumber);
        if (directPayment != null) {
            return directPayment;
        }

        // If not, check for repeating payments
        for (Map.Entry<Integer, EarlyPayment> entry : earlyPayments.entrySet()) {
            int paymentMonth = entry.getKey();
            EarlyPayment payment = entry.getValue();

            // Skip if this payment is for a future month
            if (paymentMonth > monthNumber) {
                continue;
            }

            // Check if this payment repeats to the end and applies to the current month
            if (payment.getRepeatingStrategy() == EarlyPaymentRepeatingStrategy.TO_END && paymentMonth < monthNumber) {
                return payment;
            }
        }

        return null;
    }

    private BigDecimal calculateCorrectedPaymentAmount(TaxResult taxResult) {
        // The total payment is the sum of adjusted principal, adjusted interest,
        // and tax amount if tax is excluded
        BigDecimal baseAmount = taxResult.getAdjustedPrincipalAmount()
                .add(taxResult.getAdjustedInterestAmount());

        if (taxResult.isTaxIncluded()) {
            // If tax is included, it's already part of the original amounts
            return baseAmount;
        } else {
            // If tax is excluded, we need to add it to the base amount
            return baseAmount.add(taxResult.getTotalTaxAmount());
        }
    }

    private BigDecimal calculatePaymentAmount(TaxResult taxResult) {
        if (taxResult.isTaxIncluded()) {
            // If tax is included, it's already part of the original amounts
            return taxResult.getAdjustedPrincipalAmount().add(taxResult.getAdjustedInterestAmount());
        } else {
            // If tax is excluded, we need to add it to the adjusted amounts
            return taxResult.getAdjustedPrincipalAmount()
                    .add(taxResult.getAdjustedInterestAmount())
                    .add(taxResult.getTotalTaxAmount());
        }
    }

    private BigDecimal getMonthlyInterestRate(BigDecimal rate) {
        final BigDecimal monthlyInterestRate = rate
                .divide(BigDecimal.valueOf(100), 15, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 15, RoundingMode.HALF_UP);

        LOGGER.debug("Calculated monthly interest rate: {}", monthlyInterestRate);
        return monthlyInterestRate;
    }

    /**
     * Calculates next payment date
     *
     * @param firstPaymentDate first payment date
     * @param paymentDate      payment date
     * @return next payment date
     */
    private LocalDate getNextMonthPaymentDate(LocalDate firstPaymentDate, LocalDate paymentDate) {
        LocalDate nextMonth = paymentDate.plusMonths(1);

        try {
            paymentDate = nextMonth.withDayOfMonth(firstPaymentDate.getDayOfMonth());
        } catch (DateTimeException e) {
            LOGGER.info("Cannot construct next payment date with the requested day of month. The last month day will be used instead.");
            paymentDate = nextMonth.withDayOfMonth(nextMonth.lengthOfMonth());
        }
        return paymentDate;
    }


    /**
     * Calculates total amount of early payments with strategy {@link EarlyPaymentStrategy#DECREASE_TERM}
     * until certain payment number in the schedule + remaining loan balance
     * <p>
     * This method is used for right calculation of amortization when there are different kinds of additional payments
     * and we need to include this amount in calculation of monthly payment amount
     *
     * @param loan             loan attributes
     * @param loanBalance      current loan balance
     * @param untilThisPayment current payment number
     * @return total amount of early payments + remaining loan balance
     */
    private BigDecimal getTotalAmountOfEarlyPaymentsWithLoanBalanceUntilPayment(Loan loan, BigDecimal loanBalance, int untilThisPayment) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map.Entry<Integer, EarlyPayment> entry : loan.getEarlyPayments().entrySet()) {
            if (entry.getKey() < untilThisPayment && entry.getValue().getStrategy() == EarlyPaymentStrategy.DECREASE_TERM) {
                totalAmount = totalAmount.add(entry.getValue().getAmount());
            }
        }
        totalAmount = loanBalance.add(totalAmount);
        LOGGER.info("Calculating total amount of early payments(decrease term strategy) with remaining loan balance:{}, until payment number: {}\n Result: {}",
                loanBalance, untilThisPayment, totalAmount);

        return totalAmount;
    }

    /**
     * Calculates monthly payment amount
     *
     * @param amount loan balance
     * @param rate   monthly interest rate
     * @param term   loan term in months
     * @return monthly payment amount
     */
    private BigDecimal getMonthlyPaymentAmount(BigDecimal amount, BigDecimal rate, Integer term) {
        LOGGER.info("Calculating monthly payment amount for: {}, {}, {}", amount, rate, term);

        BigDecimal monthlyPaymentAmount = getInterestAmountByBalanceAndMonthlyInterestRate(
                amount,
                (rate.multiply(BigDecimal.ONE.add(rate).pow(term)))
                        .divide((BigDecimal.ONE.add(rate).pow(term).subtract(BigDecimal.ONE)), 15, RoundingMode.HALF_UP)
        );

        LOGGER.info("Calculate monthly payment amount: {}", amount);
        return monthlyPaymentAmount;
    }

    /**
     * Calculates interest amount
     *
     * @param currentLoanBalance current loan balance
     * @param annualInterestRate interest rate
     * @param daysInMonth        days in current month
     * @param daysInYear         days in current year
     * @return interest amount
     */
    private BigDecimal getInterestAmountByBalanceRateAndDays(BigDecimal currentLoanBalance, BigDecimal annualInterestRate, int daysInMonth, int daysInYear) {
        return currentLoanBalance.multiply(
                (annualInterestRate.multiply(BigDecimal.valueOf(daysInMonth)))
                        .divide(BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(daysInYear)), 15, RoundingMode.HALF_UP)
        ).setScale(2, RoundingMode.HALF_UP);
    }


    /**
     * Calculates interest amount
     *
     * @param currentLoanBalance  current loan balance
     * @param monthlyInterestRate calculated monthly interest rate
     * @return interest amount
     */
    private BigDecimal getInterestAmountByBalanceAndMonthlyInterestRate(BigDecimal currentLoanBalance, BigDecimal monthlyInterestRate) {
        return currentLoanBalance
                .multiply(monthlyInterestRate)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates loan balance
     *
     * @param loan                loan
     * @param currentLoanBalance  current loan balance
     * @param monthlyInterestRate monthly interest rate
     * @param paymentDate         current payment date
     * @return interest amount
     */
    private BigDecimal calculateInterestAmount(Loan loan, BigDecimal currentLoanBalance, BigDecimal monthlyInterestRate, LocalDate paymentDate) {
        return /*paymentDate == null
                ?*/ getInterestAmountByBalanceAndMonthlyInterestRate(currentLoanBalance, monthlyInterestRate);
                /*: getInterestAmountByBalanceRateAndDays(
                    currentLoanBalance, loan.getRate(),
                    paymentDate.minusMonths(1).lengthOfMonth(),
                    paymentDate.minusMonths(1).lengthOfYear()
                );TODO*/
    }
}