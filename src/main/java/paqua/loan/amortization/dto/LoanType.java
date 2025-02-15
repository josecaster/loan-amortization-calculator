package paqua.loan.amortization.dto;

public enum LoanType {
    ANNUAL_BALANCED("Annual balanced"),/*FIXED_INTREST_ONLY,*/FIXED_INTREST("Monthly fixed");

    private String caption;

    LoanType(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    @Override
    public String toString() {
        return caption;
    }
}
