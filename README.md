Console-Based Banking System
This is a console-based Java application that simulates a basic banking system. It allows users to register, log in, and manage their bank accounts securely.

Features
User Authentication: Users can register and log in to their accounts. Passwords are securely hashed using SHA-256 and encoded in Base64 before being stored.

Account Types: The system utilizes Object-Oriented Programming to support two distinct account types:

Savings Account: Prevents users from withdrawing more than their available balance. It also imposes a strict maximum withdrawal limit of 1000 per transaction.

Checking Account: Provides an overdraft buffer, permitting users to withdraw up to 500 beyond their current balance.

Financial Operations: Once authenticated, users can check their balance, deposit funds, and withdraw money based on their account's specific rules.

Transaction History: The system records all deposits and withdrawals as boolean values (true for deposit, false for withdrawal) and specific amounts. Users can easily view their complete transaction history.

Technical Stack
Language: Java

Database: MySQL via JDBC

Security: java.security.MessageDigest

Setup Requirements
To run this project locally, you will need to set up a MySQL database:

Database Name: The application connects to a database named Bank.

Database Connection: The default configuration expects the database to be running on localhost:3306.

Credentials: The application uses the default username root and the password 1234.

Database Schema: You must create three tables for the application to function correctly:

users (requires id, username, password).

accounts (requires user_id, balance, type).

transactions (requires user_id, type, amount).
