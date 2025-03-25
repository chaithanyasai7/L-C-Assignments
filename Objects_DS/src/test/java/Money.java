public class Money {
    private final float amount;

    public Money(float amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative.");
        }
        this.amount = amount;
    }

    public float getAmount() {
        return amount;
    }

    public Money add(Money other) {
        return new Money(this.amount + other.amount);
    }

    public Money subtract(Money other) {
        if (this.amount < other.amount) {
            throw new IllegalArgumentException("Insufficient funds.");
        }
        return new Money(this.amount - other.amount);
    }

    @Override
    public String toString() {
        return "$" + amount;
    }
}