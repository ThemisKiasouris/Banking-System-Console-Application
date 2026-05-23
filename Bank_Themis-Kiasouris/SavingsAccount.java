public class SavingsAccount extends Account {

    // constructor to initialize userId and balance
    public SavingsAccount(int userId, double balance) {
        super(userId, balance);
    }

    // withdraw method
    @Override
    public void withdraw(double amount) {
        if (amount > getBalance()) {
            throw new IllegalArgumentException("Not enough balance!");
        }
        if (amount > 1000) {
            throw new IllegalArgumentException("Max withdraw: 1000");
        }
        setBalance(getBalance() - amount);
    }
}