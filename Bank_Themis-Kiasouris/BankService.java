import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Base64;
import java.util.Scanner;

public class BankService {
    private static Scanner scanner = new Scanner(System.in);
    private static User currentUser = null;

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/Bank";
        String username = "root";
        String password = "1234";

        try (Connection con = DriverManager.getConnection(url, username, password)) {
            while (true) {
                if (currentUser == null) {
                    showMenu(con);
                } else {
                    showUserMenu(con);
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error.");
            e.printStackTrace();
        }
    }

    private static void showMenu(Connection con) throws SQLException {
        System.out.println("\n1. Login\n2. Register\n0. Exit");
        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        if (choice == 1) login(con);
        else if (choice == 2) register(con);
        else System.exit(0);
    }

    private static void register(Connection con) throws SQLException {
        System.out.print("Username: ");
        String name = scanner.nextLine();
        System.out.print("Password: ");
        String pass = scanner.nextLine();
        // hashed password
        String hashedpass = hashPassword(pass);
        System.out.print("Type (1=Savings, 2=Checking): ");
        int type = scanner.nextInt();

        try {
            // Save to users table
            String userQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
            try (PreparedStatement psUser = con.prepareStatement(userQuery, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, name);
                psUser.setString(2, hashedpass);
                psUser.executeUpdate();

                ResultSet rs = psUser.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    // Save to accounts table with 0 balance
                    String accSql = "INSERT INTO accounts (user_id, balance, type) VALUES (?, 0, ?)";
                    try (PreparedStatement psAcc = con.prepareStatement(accSql)) {
                        psAcc.setInt(1, userId);
                        psAcc.setInt(2, type );
                        psAcc.executeUpdate();
                        System.out.println("Registered successfully!");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Register error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void login(Connection con) throws SQLException {
        System.out.print("Username: ");
        String name = scanner.nextLine();
        System.out.print("Password: ");
        String pass = scanner.nextLine();
        String hashpass = hashPassword(pass);

        String logQuery = "SELECT id FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement ps = con.prepareStatement(logQuery)) {
            ps.setString(1, name);
            ps.setString(2, hashpass);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                currentUser = new User(rs.getInt("id"), name);
                System.out.println("Login successful!");
            } else {
                System.out.println("Wrong credentials");
            }
        }
    }
    private static void showUserMenu(Connection con) throws SQLException {
        System.out.println("\n1. Balance\n2. Deposit\n3. Withdraw\n4. Transactions\n0. Logout");
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                checkBalance(con);
                break;
            case 2:
                deposit(con);
                break;
            case 3:
                withdraw(con);
                break;
            case 4:
                showTransactions(con);
                break;
            case 0:
                currentUser = null;
                System.out.println("Logged out successfully.");
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private static void checkBalance(Connection con) throws SQLException {
        String query = "SELECT balance FROM accounts WHERE user_id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, currentUser.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Your current balance is: €" + rs.getDouble("balance"));
            } else {
                System.out.println("Account not found.");
            }
        }
    }

    private static void deposit(Connection con) throws SQLException {
        System.out.print("Enter amount to deposit: ");
        double amount = scanner.nextDouble();

        // 1. Update balance in accounts table
        String updateQuery = "UPDATE accounts SET balance = balance + ? WHERE user_id = ?";
        try (PreparedStatement ps = con.prepareStatement(updateQuery)) {
            ps.setDouble(1, amount);
            ps.setInt(2, currentUser.getId());
            ps.executeUpdate();
        }

        // 2. Log in transactions table
        String transQuery = "INSERT INTO transactions (user_id, type, amount) VALUES (?, true, ?)";
        try (PreparedStatement ps = con.prepareStatement(transQuery)) {
            ps.setInt(1, currentUser.getId());
            ps.setDouble(2, amount);
            ps.executeUpdate();
        }
        System.out.println("Deposit successful!");
    }

    private static void withdraw(Connection con) throws SQLException {
        System.out.print("Enter amount to withdraw: ");
        double amount = scanner.nextDouble();

        // 1. Fetch current balance and account type from the database
        String query = "SELECT balance, type FROM accounts WHERE user_id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, currentUser.getId());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double dbBalance = rs.getDouble("balance");
                int accType = rs.getInt("type"); // 1 = Savings, 2 = Checking

                try {
                    Account account;
                    if (accType == 1) {
                        account = new SavingsAccount(currentUser.getId(), dbBalance);
                    } else {
                        account = new CheckingAccount(currentUser.getId(), dbBalance);
                    }

                    // 3. Attempt the withdrawal
                    account.withdraw(amount);

                    // 4. Update the database ONLY if the withdrawal passed the check above
                    String updateDb = "UPDATE accounts SET balance = ? WHERE user_id = ?";
                    try (PreparedStatement psUpdate = con.prepareStatement(updateDb)) {
                        psUpdate.setDouble(1, account.getBalance()); // Get the newly calculated balance
                        psUpdate.setInt(2, currentUser.getId());
                        psUpdate.executeUpdate();
                    }

                    // 5. Log the transaction in the history
                    String logTx = "INSERT INTO transactions (user_id, type, amount) VALUES (?, false, ?)";
                    try (PreparedStatement psTx = con.prepareStatement(logTx)) {
                        psTx.setInt(1, currentUser.getId());
                        psTx.setDouble(2, amount);
                        psTx.executeUpdate();
                    }
                    System.out.println("Withdrawal successful! Your new balance is: €" + account.getBalance());

                } catch (IllegalArgumentException e) {
                    // This catches the specific error messages
                    System.out.println("Error: " + e.getMessage());
                }
            } else {
                System.out.println("Account not found.");
            }
        }
    }

    private static void showTransactions(Connection con) throws SQLException {
        String query = "SELECT type, amount FROM transactions WHERE user_id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, currentUser.getId());
            ResultSet rs = ps.executeQuery();

            System.out.println("\n--- Transaction History ---");
            boolean hasTransactions = false;
            while (rs.next()) {
                hasTransactions = true;
                // Read the boolean and translate it to a String
                boolean isDeposit = rs.getBoolean("type");
                String typeName = isDeposit ? "Deposit" : "Withdrawal";

                System.out.println(typeName + ": $" + rs.getDouble("amount"));
            }
            if (!hasTransactions) {
                System.out.println("No transactions found.");
            }
        }
    }

    // Helper method to hash passwords using SHA-256
    private static String hashPassword(String plainTextPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainTextPassword.getBytes());
            // Convert the byte array into a readable String that fits in the database
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

}