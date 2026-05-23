public class Account {
    private int userId;
    private double balance;

    // constructor to initialize userId and balance
    public Account(int userId, double balance) {
        this.userId = userId;
        this.balance = balance;
    }

    // setters and getters for userId and balance
    public int getUserId() {

        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

    // method to return the current balance
    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    // method to deposit money into the account
    public void deposit(double amount) {

        balance += amount;
    }

    // method to withdraw money from the account
    public void withdraw(double amount) {
        if (balance >= amount) {
            balance -= amount;
        } else {
            System.out.println("Not enough balance in your account.");
        }
    }
}
