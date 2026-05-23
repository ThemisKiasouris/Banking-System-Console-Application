public class Transaction {
    private boolean type;
    private double amount;

    // constructor to initialize type and amount
    public Transaction(boolean type, double amount) {
        this.type = type;
        this.amount = amount;
    }

    // getters and setters for type and amount
    public boolean isType() {
        return type;
    }
    public void setType(boolean type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
}
