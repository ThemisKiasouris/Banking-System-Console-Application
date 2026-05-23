public class CheckingAccount extends Account {

    // constructor to initialize userId and balance
    public CheckingAccount(int userId, double balance) {
        super(userId, balance);
    }

    @Override
    public void withdraw(double amount) {
        if (amount > getBalance() + 500) {
            throw new IllegalArgumentException("Overdraft limit exceeded!");
        }
        setBalance(getBalance() - amount);
    }
}