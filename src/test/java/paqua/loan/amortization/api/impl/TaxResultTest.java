package paqua.loan.amortization.api.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.jupiter.api.Test;
import paqua.loan.amortization.dto.LoanTaxType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class for the TaxResult class.
 * This class tests various tax calculation scenarios for loan payments.
 */
class TaxResultTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Test
    void shouldCalculateTaxForInterestOnlyIncluded() throws IOException {
        // Given
        BigDecimal monthlyInterest = BigDecimal.valueOf(1000.00);
        BigDecimal monthlyPrincipal = BigDecimal.valueOf(5000.00);
        BigDecimal taxRate = BigDecimal.valueOf(21.00);
        Boolean taxIncluded = true;
        LoanTaxType loanTaxType = LoanTaxType.INTREST_ONLY;

        // When
        TaxResult result = TaxResult.calculate(taxIncluded, loanTaxType, taxRate, monthlyInterest, monthlyPrincipal);
        result.printLog();

        // Then
        assertNotNull(result);

        TaxResult reference = OBJECT_MAPPER.readValue(
                new File("src/test/resources/tax/reference-tax-interest-only-included.json"),
                TaxResult.class
        );

        assertEquals(reference.getTotalTaxAmount(), result.getTotalTaxAmount());
        assertEquals(reference.getAdjustedInterestAmount(), result.getAdjustedInterestAmount());
        assertEquals(reference.getAdjustedPrincipalAmount(), result.getAdjustedPrincipalAmount());
        assertEquals(reference.getInterestTaxAmount(), result.getInterestTaxAmount());
        assertEquals(reference.getPrincipalTaxAmount(), result.getPrincipalTaxAmount());
    }

    @Test
    void shouldCalculateTaxForInterestOnlyExcluded() throws IOException {
        // Given
        BigDecimal monthlyInterest = BigDecimal.valueOf(1000.00);
        BigDecimal monthlyPrincipal = BigDecimal.valueOf(5000.00);
        BigDecimal taxRate = BigDecimal.valueOf(21.00);
        Boolean taxIncluded = false;
        LoanTaxType loanTaxType = LoanTaxType.INTREST_ONLY;

        // When
        TaxResult result = TaxResult.calculate(taxIncluded, loanTaxType, taxRate, monthlyInterest, monthlyPrincipal);
        result.printLog();

        // Then
        assertNotNull(result);

        TaxResult reference = OBJECT_MAPPER.readValue(
                new File("src/test/resources/tax/reference-tax-interest-only-excluded.json"),
                TaxResult.class
        );

        assertEquals(reference.getTotalTaxAmount(), result.getTotalTaxAmount());
        assertEquals(reference.getAdjustedInterestAmount(), result.getAdjustedInterestAmount());
        assertEquals(reference.getAdjustedPrincipalAmount(), result.getAdjustedPrincipalAmount());
        assertEquals(reference.getInterestTaxAmount(), result.getInterestTaxAmount());
        assertEquals(reference.getPrincipalTaxAmount(), result.getPrincipalTaxAmount());
    }

    @Test
    void shouldCalculateTaxForBothIncluded() throws IOException {
        // Given
        BigDecimal monthlyInterest = BigDecimal.valueOf(1000.00);
        BigDecimal monthlyPrincipal = BigDecimal.valueOf(5000.00);
        BigDecimal taxRate = BigDecimal.valueOf(21.00);
        Boolean taxIncluded = true;
        LoanTaxType loanTaxType = LoanTaxType.BOTH;

        // When
        TaxResult result = TaxResult.calculate(taxIncluded, loanTaxType, taxRate, monthlyInterest, monthlyPrincipal);
        result.printLog();

        // Then
        assertNotNull(result);

        TaxResult reference = OBJECT_MAPPER.readValue(
                new File("src/test/resources/tax/reference-tax-both-included.json"),
                TaxResult.class
        );

        assertEquals(reference.getTotalTaxAmount(), result.getTotalTaxAmount());
        assertEquals(reference.getAdjustedInterestAmount(), result.getAdjustedInterestAmount());
        assertEquals(reference.getAdjustedPrincipalAmount(), result.getAdjustedPrincipalAmount());
        assertEquals(reference.getInterestTaxAmount(), result.getInterestTaxAmount());
        assertEquals(reference.getPrincipalTaxAmount(), result.getPrincipalTaxAmount());
    }

    @Test
    void shouldCalculateTaxForBothExcluded() throws IOException {
        // Given
        BigDecimal monthlyInterest = BigDecimal.valueOf(1000.00);
        BigDecimal monthlyPrincipal = BigDecimal.valueOf(5000.00);
        BigDecimal taxRate = BigDecimal.valueOf(21.00);
        Boolean taxIncluded = false;
        LoanTaxType loanTaxType = LoanTaxType.BOTH;

        // When
        TaxResult result = TaxResult.calculate(taxIncluded, loanTaxType, taxRate, monthlyInterest, monthlyPrincipal);
        result.printLog();

        // Then
        assertNotNull(result);

        TaxResult reference = OBJECT_MAPPER.readValue(
                new File("src/test/resources/tax/reference-tax-both-excluded.json"),
                TaxResult.class
        );

        assertEquals(reference.getTotalTaxAmount(), result.getTotalTaxAmount());
        assertEquals(reference.getAdjustedInterestAmount(), result.getAdjustedInterestAmount());
        assertEquals(reference.getAdjustedPrincipalAmount(), result.getAdjustedPrincipalAmount());
        assertEquals(reference.getInterestTaxAmount(), result.getInterestTaxAmount());
        assertEquals(reference.getPrincipalTaxAmount(), result.getPrincipalTaxAmount());
    }

    @Test
    void shouldCalculateTaxForPrincipalOnlyIncluded() throws IOException {
        // Given
        BigDecimal monthlyInterest = BigDecimal.valueOf(1000.00);
        BigDecimal monthlyPrincipal = BigDecimal.valueOf(5000.00);
        BigDecimal taxRate = BigDecimal.valueOf(21.00);
        Boolean taxIncluded = true;
        LoanTaxType loanTaxType = LoanTaxType.PRINCIPAL_ONLY;

        // When
        TaxResult result = TaxResult.calculate(taxIncluded, loanTaxType, taxRate, monthlyInterest, monthlyPrincipal);
        result.printLog();

        // Then
        assertNotNull(result);

        TaxResult reference = OBJECT_MAPPER.readValue(
                new File("src/test/resources/tax/reference-tax-principal-only-included.json"),
                TaxResult.class
        );

        assertEquals(reference.getTotalTaxAmount(), result.getTotalTaxAmount());
        assertEquals(reference.getAdjustedInterestAmount(), result.getAdjustedInterestAmount());
        assertEquals(reference.getAdjustedPrincipalAmount(), result.getAdjustedPrincipalAmount());
        assertEquals(reference.getInterestTaxAmount(), result.getInterestTaxAmount());
        assertEquals(reference.getPrincipalTaxAmount(), result.getPrincipalTaxAmount());
    }

    @Test
    void shouldCalculateTaxForPrincipalOnlyExcluded() throws IOException {
        // Given
        BigDecimal monthlyInterest = BigDecimal.valueOf(1000.00);
        BigDecimal monthlyPrincipal = BigDecimal.valueOf(5000.00);
        BigDecimal taxRate = BigDecimal.valueOf(21.00);
        Boolean taxIncluded = false;
        LoanTaxType loanTaxType = LoanTaxType.PRINCIPAL_ONLY;

        // When
        TaxResult result = TaxResult.calculate(taxIncluded, loanTaxType, taxRate, monthlyInterest, monthlyPrincipal);
        result.printLog();

        // Then
        assertNotNull(result);

        TaxResult reference = OBJECT_MAPPER.readValue(
                new File("src/test/resources/tax/reference-tax-principal-only-excluded.json"),
                TaxResult.class
        );

        assertEquals(reference.getTotalTaxAmount(), result.getTotalTaxAmount());
        assertEquals(reference.getAdjustedInterestAmount(), result.getAdjustedInterestAmount());
        assertEquals(reference.getAdjustedPrincipalAmount(), result.getAdjustedPrincipalAmount());
        assertEquals(reference.getInterestTaxAmount(), result.getInterestTaxAmount());
        assertEquals(reference.getPrincipalTaxAmount(), result.getPrincipalTaxAmount());
    }

    @Test
    void shouldCalculateAdditionalPaymentWithTaxBothIncluded() {
        // Given
        BigDecimal additionalPayment = BigDecimal.valueOf(10000.00);
        BigDecimal taxRate = BigDecimal.valueOf(21.00);
        Boolean taxIncluded = true;
        LoanTaxType loanTaxType = LoanTaxType.BOTH;

        // When
        BigDecimal result = TaxResult.calculateAdditionalPayment(taxIncluded, loanTaxType, taxRate, additionalPayment);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(8264.46), result);
    }

    @Test
    void shouldCalculateAdditionalPaymentWithTaxPrincipalOnlyIncluded() {
        // Given
        BigDecimal additionalPayment = BigDecimal.valueOf(10000.00);
        BigDecimal taxRate = BigDecimal.valueOf(21.00);
        Boolean taxIncluded = true;
        LoanTaxType loanTaxType = LoanTaxType.PRINCIPAL_ONLY;

        // When
        BigDecimal result = TaxResult.calculateAdditionalPayment(taxIncluded, loanTaxType, taxRate, additionalPayment);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(8264.46), result); // Now it should handle PRINCIPAL_ONLY
    }

    @Test
    void shouldNotAdjustAdditionalPaymentWhenNoTaxConfig() {
        // Given
        BigDecimal additionalPayment = BigDecimal.valueOf(10000.00);
        Boolean taxIncluded = null;
        LoanTaxType loanTaxType = null;
        BigDecimal taxRate = null;

        // When
        BigDecimal result = TaxResult.calculateAdditionalPayment(taxIncluded, loanTaxType, taxRate, additionalPayment);

        // Then
        assertNotNull(result);
        assertEquals(additionalPayment, result); // Should not change when no tax config
    }

    @Test
    void shouldCalculateExtractIncludedTax() {
        // Given
        BigDecimal amountWithTax = BigDecimal.valueOf(1000.00);
        BigDecimal taxRate = BigDecimal.valueOf(21.00);

        // When
        BigDecimal taxAmount = TaxResult.extractIncludedTax(taxRate, amountWithTax);

        // Then
        assertNotNull(taxAmount);
        assertEquals(BigDecimal.valueOf(173.55), taxAmount);
    }

    @Test
    void shouldCalculateExcludedTax() {
        // Given
        BigDecimal baseAmount = BigDecimal.valueOf(1000.00);
        BigDecimal taxRate = BigDecimal.valueOf(21.00);

        // When
        BigDecimal taxAmount = TaxResult.calculateExcludedTax(taxRate, baseAmount);

        // Then
        assertNotNull(taxAmount);
        assertEquals("210.00", taxAmount.toString());
    }
}