package Utils;

/**
 * Represents a resource with a limited capacity and current amount.
 * Provides methods to modify, restore, and query the resource's state,
 * ensuring the amount does not exceed its capacity or drop below zero.
 *
 * Inspired by Tal barami (We loved the idea of resourse)
 */
public class Resource {

    private int capacity;
    private int amount;

    public Resource(int capacity, int amount) {
        this.capacity = capacity;
        this.amount = amount;
    }

    public int getCapacity() { return  capacity; }

    public void addCapacity(int capacity) { this.capacity += capacity; }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) { this.amount = Math.min(amount,capacity); }

    public void addAmount(int amount) { this.amount = Math.min((this.amount + amount),capacity); }

    public void reduceAmount(int amount) { this.amount = Math.max(this.amount - amount, 0); }

    public void restore() {
        this.amount = this.capacity;
    }

    public String toString() {
        return String.format("%d/%d", amount,capacity);
    }

}
