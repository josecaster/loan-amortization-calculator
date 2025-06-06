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
package paqua.loan.amortization.dto;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * This class represent input attributes of loan
 *
 * @author Artyom Panfutov
 */
public final class Loan implements Serializable {
    private static final long serialVersionUID = 8435495249049946452L;

    private final LoanType loanType;

    /**
     * Debt amount (principal)
     */
    private final BigDecimal amount;

    /**
     * Interest rate
     */
    private final BigDecimal rate;

    /**
     * Loan term in months
     */
    private final Integer term;


    /**
     * Early payments (or additional payments)
     *
     * Key: number of payment in payment schedule (starts with 0)
     * Value: early payment data(amount, strategy)
     */
    private final Map<Integer, EarlyPayment> earlyPayments;

    /**
     * First payment date (optional)
     */
    private final LocalDate firstPaymentDate;

    /**
     * Tax Percentage (optional)
     */
    private final BigDecimal taxPercentage;

    /**
     * Tax Type knowing what to calculate tax on (optional)
     */
    private final LoanTaxType loanTaxType;

    /**
     * Tax included? (optional)
     */
    private final Boolean taxDeductible;
    /**
     * Products that make up the loan (optional)
     */
    private final List<Item> items;

    @ConstructorProperties({"amount", "rate", "term", "earlyPayments", "firstPaymentDate", "loanType", "taxPercentage", "loanTaxType", "taxDeductible", "products"})
    public Loan(BigDecimal amount, BigDecimal rate, Integer term, Map<Integer, EarlyPayment> earlyPayments, LocalDate firstPaymentDate, LoanType loanType, BigDecimal taxPercentage, LoanTaxType loanTaxType, Boolean taxDeductible, List<Item> items) {
        this.amount = amount;
        this.rate = rate;
        this.term = term;
        this.earlyPayments = earlyPayments;
        this.firstPaymentDate = firstPaymentDate;
        this.loanType = loanType;
        this.taxPercentage = taxPercentage;
        this.loanTaxType = loanTaxType;
        this.taxDeductible = taxDeductible;
        this.items = items;
    }

    /**
     * @return Debt amount (principal)
     */
    public BigDecimal getAmount() {
        return amount;
    }


    /**
     * @return Interest rate
     */
    public BigDecimal getRate() {
        return rate;
    }

    /**
     * @return Loan term in months
     */
    public Integer getTerm() {
        return term;
    }

    /**
     * Early payments (or additional payments)
     *
     * Key: number of payment in payment schedule
     * Value: early payment data(amount, strategy)
     *
     * @return Early payments
     */
    public Map<Integer, EarlyPayment> getEarlyPayments() {
        return earlyPayments;
    }

    /**
     * @return First payment date
     */
    public LocalDate getFirstPaymentDate() {
        return firstPaymentDate;
    }

    public LoanType getLoanType() {
        return loanType;
    }

    public BigDecimal getTaxPercentage() {
        return taxPercentage;
    }

    public LoanTaxType getLoanTaxType() {
        return loanTaxType;
    }

    /**
     * @return Products that make up the loan
     */
    public List<Item> getProducts() {
        return items != null ? Collections.unmodifiableList(items) : Collections.emptyList();
    }

    public Boolean getTaxDeductible() {
        return taxDeductible;
    }

    public static LoanBuilder builder() {
        return new LoanBuilder();
    }

    /**
     * Builder class for Loan
     */
    public static final class LoanBuilder  {

        private BigDecimal amount;
        private BigDecimal rate;
        private Integer term;
        private Map<Integer, EarlyPayment> earlyPayments;
        private LocalDate firstPaymentDate;
        private LoanType loanType = LoanType.ANNUAL_BALANCED;
        private BigDecimal taxPercentage;
        private LoanTaxType loanTaxType;
        private Boolean includeTax;// null for no tax, true for include, false for exclude
        private List<Item> items;

        public LoanBuilder() {
        }

        public LoanBuilder(BigDecimal amount, BigDecimal rate, Integer term, Map<Integer, EarlyPayment> earlyPayments, LocalDate firstPaymentDate, LoanType loanType, BigDecimal taxPercentage, LoanTaxType loanTaxType, Boolean includeTax, List<Item> items) {
            this.amount = amount;
            this.rate = rate;
            this.term = term;
            this.earlyPayments = earlyPayments;
            this.firstPaymentDate = firstPaymentDate;
            this.loanType = loanType == null ? LoanType.ANNUAL_BALANCED : loanType;
            this.taxPercentage = taxPercentage;
            this.loanTaxType = loanTaxType;
            this.includeTax = includeTax;
            this.items = items;
        }

        /**
         * Sets dept amount (principal) in BigDecimal
         *
         * @param amount loan amount
         * @return loan builder
         */
        public LoanBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

		/**
		 * Sets dept amount (principal) in double
		 *
		 * @param amount loan amount
		 * @return amount
		 */
		public LoanBuilder amount(double amount) {
			this.amount = BigDecimal.valueOf(amount);
			return this;
		}

		/**
         * Sets interest rate
         *
         * @param rate interest rate in BigDecimal
         * @return loan builder
         */
        public LoanBuilder rate(BigDecimal rate) {
            this.rate = rate;
            return this;
        }

		/**
		 * Sets interest rate in double
		 * @param rate interest rate
		 * @return loan builder
		 */
		public LoanBuilder rate(double rate) {
			this.rate = BigDecimal.valueOf(rate);
			return this;
		}

        /**
         * Sets loan term in months
         *
         * @param term (months)
         * @return loan builder
         */
        public LoanBuilder term(Integer term) {
            this.term = term;
            return this;
        }

        /**
         * Sets loan taxPercentage
         *
         * @param taxPercentage
         * @return loan builder
         */
        public LoanBuilder tax(BigDecimal taxPercentage) {
            this.taxPercentage = taxPercentage;
            return this;
        }

        /**
         * Sets loan loanTaxType
         *
         * @param loanTaxType
         * @return loan builder
         */
        public LoanBuilder tax(LoanTaxType loanTaxType) {
            this.loanTaxType = loanTaxType;
            return this;
        }

        /**
         * Sets loan includeTax
         *
         * @param includeTax
         * @return loan builder
         */
        public LoanBuilder tax(Boolean includeTax) {
            this.includeTax = includeTax;
            return this;
        }

        /**
         * Sets early payment map
         * @param earlyPayments early payments map where key is a number of the payment, value - an early payment
         *
         * @return loan builder
         */
        public LoanBuilder earlyPayments(Map<Integer, EarlyPayment> earlyPayments) {
            this.earlyPayments = earlyPayments;
            return this;
        }

        /**
         * Adds one early payment to the early payments map
         * @param number number of the payment
         * @param earlyPayment early payment
         * @return loan builder
         */
        public LoanBuilder earlyPayment(int number, EarlyPayment earlyPayment) {
            if (this.earlyPayments == null) {
                this.earlyPayments = new HashMap<>();
            }

            this.earlyPayments.put(number, earlyPayment);
            return this;
        }

        /**
         * Sets first payment date
         *
         * @param firstPaymentDate date of the first payment
         *
         * @return loan builder
         */
        public LoanBuilder firstPaymentDate(LocalDate firstPaymentDate) {
            this.firstPaymentDate = firstPaymentDate;
            return this;
        }

        public LoanBuilder setLoanType(LoanType loanType) {
            this.loanType = loanType;
            return this;
        }

        /**
         * Sets products that make up the loan
         *
         * @param items list of products
         * @return loan builder
         */
        public LoanBuilder products(List<Item> items) {
            this.items = items;
            return this;
        }

        public Loan build() {
            return new Loan(amount, rate, term, earlyPayments, firstPaymentDate,loanType, taxPercentage, loanTaxType, includeTax, items);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loan loan = (Loan) o;
        return Objects.equals(amount, loan.amount) &&
                Objects.equals(rate, loan.rate) &&
                Objects.equals(term, loan.term) &&
                Objects.equals(firstPaymentDate, loan.firstPaymentDate) &&
                Objects.equals(earlyPayments, loan.earlyPayments) &&
                Objects.equals(loanType, loan.loanType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, rate, term, firstPaymentDate, earlyPayments, loanType);
    }

    @Override
    public String toString() {
        return "Loan{" +
                "amount=" + amount +
                ", rate=" + rate +
                ", term=" + term +
                ", firstPaymentDate=" + firstPaymentDate +
                ", earlyPayments=" + earlyPayments +
                ", loanType=" + loanType +
                '}';
    }
}
