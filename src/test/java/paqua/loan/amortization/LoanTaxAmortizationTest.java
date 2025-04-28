package paqua.loan.amortization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paqua.loan.amortization.api.LoanAmortizationCalculator;
import paqua.loan.amortization.api.impl.LoanAmortizationCalculatorFactory;
import paqua.loan.amortization.api.impl.repeating.EarlyPaymentRepeatingStrategy;
import paqua.loan.amortization.dto.*;
import paqua.loan.amortization.utils.factory.ObjectMapperFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration tests for Loan Calculator with tax scenarios
 */
class LoanTaxAmortizationTest {
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.create();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private LoanAmortizationCalculator calculator;

    @BeforeEach
    public void initTarget() {
        calculator = LoanAmortizationCalculatorFactory.create();
    }

    @Test
    void shouldCalculateWithProducts() throws IOException {
        // Basic loan with interest-only tax included
        Loan loan = Loan.builder()
                .products(Collections.singletonList(Item.builder().id("1000").name("Test product").amount(500000.32).tax(BigDecimal.valueOf(10)).build()))
                .amount(BigDecimal.valueOf(500000.32))
                .rate(BigDecimal.valueOf(4.56))
                .term(32)
                .tax(BigDecimal.valueOf(21.0))
                .tax(LoanTaxType.INTREST_ONLY)
                .tax(true)
                .build();

        LoanAmortization amortization = calculator.calculate(loan);
        assertNotNull(amortization);
        amortization.printLog();
        System.out.println(OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(amortization));
        LoanAmortization reference = OBJECT_MAPPER.readValue(
                new File("src/test/resources/items/item-tax.json"),
                LoanAmortization.class);

        assertEquals(reference, amortization);
    }

    @Test
    void shouldCalculateWithMultipleProducts() throws IOException {
        // Basic loan with interest-only tax included
        List<Item> testProduct = new ArrayList<>();
        testProduct.add(Item.builder().id("1000").name("Test product 1").amount(400000).tax(BigDecimal.valueOf(10)).build());
        testProduct.add(Item.builder().id("2000").name("Test product 2").amount(100000).tax(BigDecimal.valueOf(10)).build());
        testProduct.add(Item.builder().id("3000").name("Test product 2").amount(0.32).build());
        Loan loan = Loan.builder()
                .products(testProduct)
                .amount(BigDecimal.valueOf(500000.32))
                .rate(BigDecimal.valueOf(4.56))
                .term(32)
                .tax(BigDecimal.valueOf(21.0))
                .tax(LoanTaxType.INTREST_ONLY)
                .tax(true)
                .build();

        LoanAmortization amortization = calculator.calculate(loan);
        assertNotNull(amortization);
        amortization.printLog();
//        System.out.println(OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(amortization));
        LoanAmortization reference = OBJECT_MAPPER.readValue(
                new File("src/test/resources/items/multiple-items-tax-one-no-tax.json"),
                LoanAmortization.class);

        assertEquals(reference, amortization);
    }

    @Test
    void shouldCalculateWithInterestOnlyTaxIncluded() throws IOException {
        // Basic loan with interest-only tax included
        Loan loan = Loan.builder()
                .amount(BigDecimal.valueOf(500000.32))
                .rate(BigDecimal.valueOf(4.56))
                .term(32)
                .tax(BigDecimal.valueOf(21.0))
                .tax(LoanTaxType.INTREST_ONLY)
                .tax(true)
                .build();

        LoanAmortization amortization = calculator.calculate(loan);
        assertNotNull(amortization);
        amortization.printLog();
        LoanAmortization reference = OBJECT_MAPPER.readValue(
                new File("src/test/resources/loantax/reference-loan-interest-only-tax-included.json"),
                LoanAmortization.class);

        assertEquals(reference, amortization);
    }

    @Test
    void shouldCalculateWithInterestOnlyTaxExcluded() throws IOException {
        // Basic loan with interest-only tax excluded
        Loan loan = Loan.builder()
                .amount(BigDecimal.valueOf(500000.32))
                .rate(BigDecimal.valueOf(4.56))
                .term(32)
                .tax(BigDecimal.valueOf(21.0))
                .tax(LoanTaxType.INTREST_ONLY)
                .tax(false)
                .build();

        LoanAmortization amortization = calculator.calculate(loan);
        assertNotNull(amortization);
        amortization.printLog();

        LoanAmortization reference = OBJECT_MAPPER.readValue(
                new File("src/test/resources/loantax/reference-loan-interest-only-tax-excluded.json"),
                LoanAmortization.class);

        assertEquals(reference, amortization);
    }

    @Test
    void shouldCalculateWithPrincipalOnlyTaxIncluded() throws IOException {
        // Basic loan with principal-only tax included
        Loan loan = Loan.builder()
                .amount(BigDecimal.valueOf(500000.32))
                .rate(BigDecimal.valueOf(4.56))
                .term(32)
                .tax(BigDecimal.valueOf(21.0))
                .tax(LoanTaxType.PRINCIPAL_ONLY)
                .tax(true)
                .build();

        LoanAmortization amortization = calculator.calculate(loan);
        assertNotNull(amortization);
        amortization.printLog();

        LoanAmortization reference = OBJECT_MAPPER.readValue(
                new File("src/test/resources/loantax/reference-loan-principal-only-tax-included.json"),
                LoanAmortization.class);

        assertEquals(reference, amortization);
    }

    @Test
    void shouldCalculateWithPrincipalOnlyTaxExcluded() throws IOException {
        // Basic loan with principal-only tax excluded
        Loan loan = Loan.builder()
                .amount(BigDecimal.valueOf(500000.32))
                .rate(BigDecimal.valueOf(4.56))
                .term(32)
                .tax(BigDecimal.valueOf(21.0))
                .tax(LoanTaxType.PRINCIPAL_ONLY)
                .tax(false)
                .build();

        LoanAmortization amortization = calculator.calculate(loan);
        assertNotNull(amortization);

        LoanAmortization reference = OBJECT_MAPPER.readValue(
                new File("src/test/resources/loantax/reference-loan-principal-only-tax-excluded.json"),
                LoanAmortization.class);

        assertEquals(reference, amortization);
    }

    @Test
    void shouldCalculateWithBothTaxIncluded() throws IOException {
        // Basic loan with both interest and principal tax included
        Loan loan = Loan.builder()
                .amount(BigDecimal.valueOf(500000.32))
                .rate(BigDecimal.valueOf(4.56))
                .term(32)
                .tax(BigDecimal.valueOf(21.0))
                .tax(LoanTaxType.BOTH)
                .tax(true)
                .build();

        LoanAmortization amortization = calculator.calculate(loan);
        assertNotNull(amortization);

        LoanAmortization reference = OBJECT_MAPPER.readValue(
                new File("src/test/resources/loantax/reference-loan-both-tax-included.json"),
                LoanAmortization.class);

        assertEquals(reference, amortization);
    }

    @Test
    void shouldCalculateWithBothTaxExcluded() throws IOException {
        // Basic loan with both interest and principal tax excluded
        Loan loan = Loan.builder()
                .amount(BigDecimal.valueOf(500000.32))
                .rate(BigDecimal.valueOf(4.56))
                .term(32)
                .tax(BigDecimal.valueOf(21.0))
                .tax(LoanTaxType.BOTH)
                .tax(false)
                .build();

        LoanAmortization amortization = calculator.calculate(loan);
        assertNotNull(amortization);

        LoanAmortization reference = OBJECT_MAPPER.readValue(
                new File("src/test/resources/loantax/reference-loan-both-tax-excluded.json"),
                LoanAmortization.class);

        assertEquals(reference, amortization);
    }

    @Test
    void shouldCalculateWithEarlyPaymentAndInterestOnlyTaxIncluded() throws IOException {
        // Loan with early payment and interest-only tax included
        Map<Integer, EarlyPayment> earlyPayments = new HashMap<>();

        earlyPayments.put(5, new EarlyPayment(
                BigDecimal.valueOf(50000),
                EarlyPaymentStrategy.DECREASE_TERM,
                EarlyPaymentRepeatingStrategy.SINGLE,
                null));

        Loan loan = Loan.builder()
                .amount(BigDecimal.valueOf(500000.32))
                .rate(BigDecimal.valueOf(4.56))
                .term(32)
                .tax(BigDecimal.valueOf(21.0))
                .tax(LoanTaxType.INTREST_ONLY)
                .tax(true)
                .earlyPayments(earlyPayments)
                .build();

        LoanAmortization amortization = calculator.calculate(loan);
        assertNotNull(amortization);

        LoanAmortization reference = OBJECT_MAPPER.readValue(
                new File("src/test/resources/loantax/reference-loan-early-payment-interest-only-tax-included.json"),
                LoanAmortization.class);


        assertEquals(reference, amortization);
    }

    @Test
    void shouldCalculateWithEarlyPaymentAndPrincipalOnlyTaxIncluded() throws IOException {
        // Loan with early payment and principal-only tax included
        Map<Integer, EarlyPayment> earlyPayments = new HashMap<>();

        earlyPayments.put(5, new EarlyPayment(
                BigDecimal.valueOf(50000),
                EarlyPaymentStrategy.DECREASE_TERM,
                EarlyPaymentRepeatingStrategy.SINGLE,
                null));

        Loan loan = Loan.builder()
                .amount(BigDecimal.valueOf(500000.32))
                .rate(BigDecimal.valueOf(4.56))
                .term(32)
                .tax(BigDecimal.valueOf(21.0))
                .tax(LoanTaxType.PRINCIPAL_ONLY)
                .tax(true)
                .earlyPayments(earlyPayments)
                .build();

        LoanAmortization amortization = calculator.calculate(loan);
        assertNotNull(amortization);

        LoanAmortization reference = OBJECT_MAPPER.readValue(
                new File("src/test/resources/loantax/reference-loan-early-payment-principal-only-tax-included.json"),
                LoanAmortization.class);

        assertEquals(reference, amortization);
    }

    @Test
    void shouldCalculateWithRepeatingEarlyPaymentsAndBothTaxExcluded() throws IOException {
        // Loan with repeating early payments and both taxes excluded
        Map<Integer, EarlyPayment> earlyPayments = new HashMap<>();

        Map<EarlyPaymentAdditionalParameters, String> parameters = new HashMap<>();
        parameters.put(EarlyPaymentAdditionalParameters.REPEAT_TO_MONTH_NUMBER, "10");

        earlyPayments.put(5, new EarlyPayment(
                BigDecimal.valueOf(50000),
                EarlyPaymentStrategy.DECREASE_TERM,
                EarlyPaymentRepeatingStrategy.TO_CERTAIN_MONTH,
                parameters
        ));

        Loan loan = Loan.builder()
                .amount(BigDecimal.valueOf(500000.32))
                .rate(BigDecimal.valueOf(4.56))
                .term(32)
                .tax(BigDecimal.valueOf(21.0))
                .tax(LoanTaxType.BOTH)
                .tax(false)
                .earlyPayments(earlyPayments)
                .build();

        LoanAmortization amortization = calculator.calculate(loan);
        assertNotNull(amortization);

        LoanAmortization reference = OBJECT_MAPPER.readValue(
                new File("src/test/resources/loantax/reference-loan-repeating-early-payments-both-tax-excluded.json"),
                LoanAmortization.class);

        assertEquals(reference, amortization);
    }

    @Test
    void shouldCalculateWithFirstPaymentDateAndInterestOnlyTaxExcluded() throws IOException {
        // Loan with first payment date and interest-only tax excluded
        Loan loan = Loan.builder()
                .amount(BigDecimal.valueOf(1500000))
                .rate(BigDecimal.valueOf(5.32))
                .term(96)
                .tax(BigDecimal.valueOf(21.0))
                .tax(LoanTaxType.INTREST_ONLY)
                .tax(false)
                .firstPaymentDate(LocalDate.parse("2014-07-02", DATE_TIME_FORMATTER))
                .build();

        LoanAmortization amortization = calculator.calculate(loan);
        assertNotNull(amortization);

        LoanAmortization reference = OBJECT_MAPPER.readValue(
                new File("src/test/resources/loantax/reference-loan-first-payment-date-interest-only-tax-excluded.json"),
                LoanAmortization.class);

        assertEquals(reference, amortization);
    }

    @Test
    void shouldCalculateWithDifferentEarlyPaymentsAndBothTaxIncluded() throws IOException {
        // Loan with different early payments and both taxes included
        Map<Integer, EarlyPayment> earlyPayments = new HashMap<>();

        earlyPayments.put(0, new EarlyPayment(
                BigDecimal.valueOf(5000),
                EarlyPaymentStrategy.DECREASE_MONTHLY_PAYMENT,
                EarlyPaymentRepeatingStrategy.SINGLE,
                null));

        earlyPayments.put(3, new EarlyPayment(
                BigDecimal.valueOf(30000),
                EarlyPaymentStrategy.DECREASE_TERM,
                EarlyPaymentRepeatingStrategy.SINGLE,
                null));

        earlyPayments.put(6, new EarlyPayment(
                BigDecimal.valueOf(10000),
                EarlyPaymentStrategy.DECREASE_TERM,
                EarlyPaymentRepeatingStrategy.SINGLE,
                null));

        Loan loan = Loan.builder()
                .amount(BigDecimal.valueOf(500000.32))
                .rate(BigDecimal.valueOf(4.56))
                .term(32)
                .tax(BigDecimal.valueOf(21.0))
                .tax(LoanTaxType.BOTH)
                .tax(true)
                .earlyPayments(earlyPayments)
                .build();

        LoanAmortization amortization = calculator.calculate(loan);
        assertNotNull(amortization);

        LoanAmortization reference = OBJECT_MAPPER.readValue(
                new File("src/test/resources/loantax/reference-loan-different-early-payments-both-tax-included.json"),
                LoanAmortization.class);

        assertEquals(reference, amortization);
    }

    @Test
    void shouldCalculateWithHighTaxRateAndPrincipalOnlyTaxExcluded() throws IOException {
        // Loan with high tax rate (30%) and principal-only tax excluded
        Loan loan = Loan.builder()
                .amount(BigDecimal.valueOf(500000.32))
                .rate(BigDecimal.valueOf(4.56))
                .term(32)
                .tax(BigDecimal.valueOf(30.0))
                .tax(LoanTaxType.PRINCIPAL_ONLY)
                .tax(false)
                .build();

        LoanAmortization amortization = calculator.calculate(loan);
        assertNotNull(amortization);

        LoanAmortization reference = OBJECT_MAPPER.readValue(
                new File("src/test/resources/loantax/reference-loan-high-tax-rate-principal-only-tax-excluded.json"),
                LoanAmortization.class);

        assertEquals(reference, amortization);
    }

    @Test
    void shouldCalculateWithZeroTaxRate() throws IOException {
        // Loan with zero tax rate
        Loan loan = Loan.builder()
                .amount(BigDecimal.valueOf(500000.32))
                .rate(BigDecimal.valueOf(4.56))
                .term(32)
                .tax(BigDecimal.valueOf(0.0))
                .tax(LoanTaxType.BOTH)
                .tax(true)
                .build();

        LoanAmortization amortization = calculator.calculate(loan);
        assertNotNull(amortization);
        // System.out.println(OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(amortization));
        // This should match the regular loan calculation without tax
        LoanAmortization reference = OBJECT_MAPPER.readValue(
                new File("src/test/resources/loantax/reference-500000.32-4.56-32.json"),
                LoanAmortization.class);

        assertEquals(reference, amortization);
    }
}