CREATE TABLE IF NOT EXISTS customers (
    id TEXT PRIMARY KEY,
    full_name TEXT NOT NULL,
    username TEXT NOT NULL,
    password TEXT NOT NULL,
    email TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS accounts (
    account_number TEXT PRIMARY KEY,
    customer_id TEXT NOT NULL,
    account_type TEXT NOT NULL,
    balance REAL NOT NULL,
    closed INTEGER NOT NULL,
    overdraft_limit REAL,
    interest_rate REAL,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id TEXT PRIMARY KEY,
    account_number TEXT NOT NULL,
    transaction_type TEXT NOT NULL,
    amount REAL NOT NULL,
    timestamp TEXT NOT NULL,
    balance_after REAL NOT NULL,
    employee_id TEXT,
    employee_name TEXT,
    employee_username TEXT,
    employee_password TEXT,
    employee_role TEXT,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number)
);