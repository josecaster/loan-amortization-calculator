package paqua.loan.amortization.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a product that is part of a loan
 */
public class Item implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String name;
    private final BigDecimal amount, tax;

    public Item(String id, String name, BigDecimal amount, BigDecimal tax) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.tax = tax;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private BigDecimal amount, tax;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder amount(double amount) {
            this.amount = BigDecimal.valueOf(amount);
            return this;
        }

        public Builder tax(BigDecimal tax) {
            this.tax = tax;
            return this;
        }

        public Item build() {
            return new Item(id, name, amount, tax);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id) &&
                Objects.equals(name, item.name) &&
                Objects.equals(amount, item.amount) &&
                Objects.equals(tax, item.tax);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, amount, tax);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                ", tax='" + tax + '\'' +
                '}';
    }
}