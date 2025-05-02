package firstswing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Scanner;

public class mainframe {
    private static final String url = "jdbc:mysql://localhost:3306/banking_system";
    private static final String username = "root";
    private static final String password = "rahawaj@1502";
    
    private static JFrame mainFrame;
    private static JPanel mainPanel;
    private static CardLayout cardLayout;
    
    private static Connection connection;
    private static Scanner scanner;
    private static User user;
    private static Accounts accounts;
    private static AccountManager accountManager;
    
    private static String currentUserEmail;
    private static long currentAccountNumber;
    
    public static void main(String[] args) {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Establish database connection
            connection = DriverManager.getConnection(url, username, password);
            scanner = new Scanner(System.in); // Still needed for some operations
            
            // Initialize classes
            user = new User(connection, scanner);
            accounts = new Accounts(connection, scanner);
            accountManager = new AccountManager(connection, scanner);
            
            // Set up the GUI
            setupGUI();
            
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Database driver not found: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection error: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private static void setupGUI() {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create main frame
        mainFrame = new JFrame("Banking System");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(600, 400);
        mainFrame.setLocationRelativeTo(null);
        
        // Create card layout for switching between panels
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Create and add all panels
        mainPanel.add(createWelcomePanel(), "welcome");
        mainPanel.add(createRegisterPanel(), "register");
        mainPanel.add(createLoginPanel(), "login");
        mainPanel.add(createNewAccountPanel(), "newAccount");
        mainPanel.add(createDashboardPanel(), "dashboard");
        mainPanel.add(createDebitPanel(), "debit");
        mainPanel.add(createCreditPanel(), "credit");
        mainPanel.add(createTransferPanel(), "transfer");
        mainPanel.add(createBalancePanel(), "balance");
        
        // Show welcome panel first
        cardLayout.show(mainPanel, "welcome");
        
        // Add panel to frame and display
        mainFrame.add(mainPanel);
        mainFrame.setVisible(true);
    }
    
    private static JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("WELCOME TO BANKING SYSTEM", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(40, 100, 40, 100));
        
        JButton registerButton = new JButton("Register");
        JButton loginButton = new JButton("Login");
        JButton exitButton = new JButton("Exit");
        
        registerButton.setFont(new Font("Arial", Font.PLAIN, 18));
        loginButton.setFont(new Font("Arial", Font.PLAIN, 18));
        exitButton.setFont(new Font("Arial", Font.PLAIN, 18));
        
        registerButton.addActionListener(e -> cardLayout.show(mainPanel, "register"));
        
        loginButton.addActionListener(e -> cardLayout.show(mainPanel, "login"));
        
        exitButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(mainFrame, 
                    "Are you sure you want to exit?", 
                    "Exit Confirmation", 
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(mainFrame, 
                        "THANK YOU FOR USING BANKING SYSTEM!!!", 
                        "Goodbye", 
                        JOptionPane.INFORMATION_MESSAGE);
                mainFrame.dispose();
                System.exit(0);
            }
        });
        
        buttonPanel.add(registerButton);
        buttonPanel.add(loginButton);
        buttonPanel.add(exitButton);
        
        panel.add(buttonPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private static JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Register New User", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        JLabel nameLabel = new JLabel("Full Name:");
        JTextField nameField = new JTextField();
        
        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField();
        
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();
        
        JButton submitButton = new JButton("Register");
        JButton backButton = new JButton("Back");
        
        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(emailLabel);
        formPanel.add(emailField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(backButton);
        formPanel.add(submitButton);
        
        submitButton.addActionListener(e -> {
            String fullName = nameField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            
            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, 
                        "All fields are required!", 
                        "Input Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (user.user_exist(email)) {
                JOptionPane.showMessageDialog(mainFrame, 
                        "User Already Exists for this Email Address!!", 
                        "Registration Failed", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String register_query = "INSERT INTO User(full_name, email, password) VALUES(?, ?, ?)";
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(register_query);
                preparedStatement.setString(1, fullName);
                preparedStatement.setString(2, email);
                preparedStatement.setString(3, password);
                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(mainFrame, 
                            "Registration Successful!", 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                    // Clear fields
                    nameField.setText("");
                    emailField.setText("");
                    passwordField.setText("");
                    // Go to login screen
                    cardLayout.show(mainPanel, "login");
                } else {
                    JOptionPane.showMessageDialog(mainFrame, 
                            "Registration Failed!", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(mainFrame, 
                        "Database error: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        
        backButton.addActionListener(e -> {
            nameField.setText("");
            emailField.setText("");
            passwordField.setText("");
            cardLayout.show(mainPanel, "welcome");
        });
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private static JPanel createLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("User Login", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField();
        
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();
        
        JButton loginButton = new JButton("Login");
        JButton backButton = new JButton("Back");
        
        formPanel.add(emailLabel);
        formPanel.add(emailField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(backButton);
        formPanel.add(loginButton);
        
        loginButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            
            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, 
                        "Email and password are required!", 
                        "Input Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String login_query = "SELECT * FROM User WHERE email = ? AND password = ?";
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(login_query);
                preparedStatement.setString(1, email);
                preparedStatement.setString(2, password);
                ResultSet resultSet = preparedStatement.executeQuery();
                
                if (resultSet.next()) {
                    // Login successful
                    currentUserEmail = email;
                    JOptionPane.showMessageDialog(mainFrame, 
                            "Login Successful!", 
                            "Welcome", 
                            JOptionPane.INFORMATION_MESSAGE);
                    
                    // Check if user has an account
                    if (!accounts.account_exist(email)) {
                        int choice = JOptionPane.showConfirmDialog(mainFrame, 
                                "You don't have a bank account yet. Would you like to open one?", 
                                "Open Account", 
                                JOptionPane.YES_NO_OPTION);
                        if (choice == JOptionPane.YES_OPTION) {
                            cardLayout.show(mainPanel, "newAccount");
                        } else {
                            // Return to welcome if they don't want to create an account
                            emailField.setText("");
                            passwordField.setText("");
                            cardLayout.show(mainPanel, "welcome");
                        }
                    } else {
                        // Get account number and go to dashboard
                        currentAccountNumber = accounts.getAccount_number(email);
                        emailField.setText("");
                        passwordField.setText("");
                        cardLayout.show(mainPanel, "dashboard");
                    }
                } else {
                    JOptionPane.showMessageDialog(mainFrame, 
                            "Incorrect Email or Password!", 
                            "Login Failed", 
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(mainFrame, 
                        "Database error: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        
        backButton.addActionListener(e -> {
            emailField.setText("");
            passwordField.setText("");
            cardLayout.show(mainPanel, "welcome");
        });
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private static JPanel createNewAccountPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Open New Bank Account", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        JLabel nameLabel = new JLabel("Full Name:");
        JTextField nameField = new JTextField();
        
        JLabel amountLabel = new JLabel("Initial Amount:");
        JTextField amountField = new JTextField();
        
        JLabel pinLabel = new JLabel("Security Pin:");
        JPasswordField pinField = new JPasswordField();
        
        JButton createButton = new JButton("Create Account");
        JButton cancelButton = new JButton("Cancel");
        
        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(amountLabel);
        formPanel.add(amountField);
        formPanel.add(pinLabel);
        formPanel.add(pinField);
        formPanel.add(cancelButton);
        formPanel.add(createButton);
        
        createButton.addActionListener(e -> {
            String fullName = nameField.getText();
            String amountText = amountField.getText();
            String pin = new String(pinField.getPassword());
            
            if (fullName.isEmpty() || amountText.isEmpty() || pin.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, 
                        "All fields are required!", 
                        "Input Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double initialAmount;
            try {
                initialAmount = Double.parseDouble(amountText);
                if (initialAmount < 0) {
                    JOptionPane.showMessageDialog(mainFrame, 
                            "Initial amount cannot be negative", 
                            "Input Error", 
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainFrame, 
                        "Please enter a valid amount", 
                        "Input Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Insert into database
            String open_account_query = "INSERT INTO Accounts(account_number, full_name, email, balance, security_pin) VALUES(?, ?, ?, ?, ?)";
            try {
                long account_number = generateAccountNumber();
                PreparedStatement preparedStatement = connection.prepareStatement(open_account_query);
                preparedStatement.setLong(1, account_number);
                preparedStatement.setString(2, fullName);
                preparedStatement.setString(3, currentUserEmail);
                preparedStatement.setDouble(4, initialAmount);
                preparedStatement.setString(5, pin);
                
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    currentAccountNumber = account_number;
                    JOptionPane.showMessageDialog(mainFrame, 
                            "Account Created Successfully!\nYour Account Number is: " + account_number, 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                    
                    // Clear fields and go to dashboard
                    nameField.setText("");
                    amountField.setText("");
                    pinField.setText("");
                    cardLayout.show(mainPanel, "dashboard");
                } else {
                    JOptionPane.showMessageDialog(mainFrame, 
                            "Account Creation Failed!", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(mainFrame, 
                        "Database error: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> {
            nameField.setText("");
            amountField.setText("");
            pinField.setText("");
            cardLayout.show(mainPanel, "welcome");
        });
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private static long generateAccountNumber() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT account_number from Accounts ORDER BY account_number DESC LIMIT 1");
            if (resultSet.next()) {
                long last_account_number = resultSet.getLong("account_number");
                return last_account_number + 1;
            } else {
                return 10000100;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 10000100;
    }
    
    private static JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Banking Dashboard", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        JLabel accountLabel = new JLabel();
        accountLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(accountLabel, BorderLayout.SOUTH);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 0, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 100, 30, 100));
        
        JButton debitButton = new JButton("Withdraw Money");
        JButton creditButton = new JButton("Deposit Money");
        JButton transferButton = new JButton("Transfer Money");
        JButton balanceButton = new JButton("Check Balance");
        JButton logoutButton = new JButton("Logout");
        
        buttonPanel.add(debitButton);
        buttonPanel.add(creditButton);
        buttonPanel.add(transferButton);
        buttonPanel.add(balanceButton);
        buttonPanel.add(logoutButton);
        
        // Set action listeners for buttons
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                // Update account number display when dashboard is shown
                accountLabel.setText("Account Number: " + currentAccountNumber);
            }
        });
        
        debitButton.addActionListener(e -> cardLayout.show(mainPanel, "debit"));
        creditButton.addActionListener(e -> cardLayout.show(mainPanel, "credit"));
        transferButton.addActionListener(e -> cardLayout.show(mainPanel, "transfer"));
        balanceButton.addActionListener(e -> cardLayout.show(mainPanel, "balance"));
        
        logoutButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(mainFrame, 
                    "Are you sure you want to logout?", 
                    "Logout Confirmation", 
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                currentUserEmail = null;
                currentAccountNumber = 0;
                cardLayout.show(mainPanel, "welcome");
            }
        });
        
        panel.add(buttonPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private static JPanel createDebitPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Withdraw Money", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        JLabel amountLabel = new JLabel("Enter Amount:");
        JTextField amountField = new JTextField();
        
        JLabel pinLabel = new JLabel("Enter Security Pin:");
        JPasswordField pinField = new JPasswordField();
        
        JButton withdrawButton = new JButton("Withdraw");
        JButton backButton = new JButton("Back");
        
        formPanel.add(amountLabel);
        formPanel.add(amountField);
        formPanel.add(pinLabel);
        formPanel.add(pinField);
        formPanel.add(backButton);
        formPanel.add(withdrawButton);
        
        withdrawButton.addActionListener(e -> {
            String amountText = amountField.getText();
            String pin = new String(pinField.getPassword());
            
            if (amountText.isEmpty() || pin.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, 
                        "Amount and PIN are required!", 
                        "Input Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double amount;
            try {
                amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(mainFrame, 
                            "Amount must be positive", 
                            "Input Error", 
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainFrame, 
                        "Please enter a valid amount", 
                        "Input Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                connection.setAutoCommit(false);
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT * FROM Accounts WHERE account_number = ? and security_pin = ?");
                preparedStatement.setLong(1, currentAccountNumber);
                preparedStatement.setString(2, pin);
                ResultSet resultSet = preparedStatement.executeQuery();
                
                if (resultSet.next()) {
                    double current_balance = resultSet.getDouble("balance");
                    if (amount <= current_balance) {
                        String debit_query = "UPDATE Accounts SET balance = balance - ? WHERE account_number = ?";
                        PreparedStatement preparedStatement1 = connection.prepareStatement(debit_query);
                        preparedStatement1.setDouble(1, amount);
                        preparedStatement1.setLong(2, currentAccountNumber);
                        int rowsAffected = preparedStatement1.executeUpdate();
                        
                        if (rowsAffected > 0) {
                            JOptionPane.showMessageDialog(mainFrame, 
                                    "Rs." + amount + " withdrawn successfully", 
                                    "Success", 
                                    JOptionPane.INFORMATION_MESSAGE);
                            connection.commit();
                            // Clear fields and return to dashboard
                            amountField.setText("");
                            pinField.setText("");
                            cardLayout.show(mainPanel, "dashboard");
                        } else {
                            JOptionPane.showMessageDialog(mainFrame, 
                                    "Transaction Failed!", 
                                    "Error", 
                                    JOptionPane.ERROR_MESSAGE);
                            connection.rollback();
                        }
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, 
                                "Insufficient Balance!", 
                                "Transaction Failed", 
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(mainFrame, 
                            "Invalid PIN!", 
                            "Authentication Failed", 
                            JOptionPane.ERROR_MESSAGE);
                }
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                try {
                    connection.rollback();
                    connection.setAutoCommit(true);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
                JOptionPane.showMessageDialog(mainFrame, 
                        "Database error: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        
        backButton.addActionListener(e -> {
            amountField.setText("");
            pinField.setText("");
            cardLayout.show(mainPanel, "dashboard");
        });
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private static JPanel createCreditPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Deposit Money", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        JLabel amountLabel = new JLabel("Enter Amount:");
        JTextField amountField = new JTextField();
        
        JLabel pinLabel = new JLabel("Enter Security Pin:");
        JPasswordField pinField = new JPasswordField();
        
        JButton depositButton = new JButton("Deposit");
        JButton backButton = new JButton("Back");
        
        formPanel.add(amountLabel);
        formPanel.add(amountField);
        formPanel.add(pinLabel);
        formPanel.add(pinField);
        formPanel.add(backButton);
        formPanel.add(depositButton);
        
        depositButton.addActionListener(e -> {
            String amountText = amountField.getText();
            String pin = new String(pinField.getPassword());
            
            if (amountText.isEmpty() || pin.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, 
                        "Amount and PIN are required!", 
                        "Input Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double amount;
            try {
                amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(mainFrame, 
                            "Amount must be positive", 
                            "Input Error", 
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainFrame, 
                        "Please enter a valid amount", 
                        "Input Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                connection.setAutoCommit(false);
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT * FROM Accounts WHERE account_number = ? and security_pin = ?");
                preparedStatement.setLong(1, currentAccountNumber);
                preparedStatement.setString(2, pin);
                ResultSet resultSet = preparedStatement.executeQuery();
                
                if (resultSet.next()) {
                    String credit_query = "UPDATE Accounts SET balance = balance + ? WHERE account_number = ?";
                    PreparedStatement preparedStatement1 = connection.prepareStatement(credit_query);
                    preparedStatement1.setDouble(1, amount);
                    preparedStatement1.setLong(2, currentAccountNumber);
                    int rowsAffected = preparedStatement1.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(mainFrame, 
                                "Rs." + amount + " deposited successfully", 
                                "Success", 
                                JOptionPane.INFORMATION_MESSAGE);
                        connection.commit();
                        // Clear fields and return to dashboard
                        amountField.setText("");
                        pinField.setText("");
                        cardLayout.show(mainPanel, "dashboard");
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, 
                                "Transaction Failed!", 
                                "Error", 
                                JOptionPane.ERROR_MESSAGE);
                        connection.rollback();
                    }
                } else {
                    JOptionPane.showMessageDialog(mainFrame, 
                            "Invalid PIN!", 
                            "Authentication Failed", 
                            JOptionPane.ERROR_MESSAGE);
                }
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                try {
                    connection.rollback();
                    connection.setAutoCommit(true);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
                JOptionPane.showMessageDialog(mainFrame, 
                        "Database error: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        
        backButton.addActionListener(e -> {
            amountField.setText("");
            pinField.setText("");
            cardLayout.show(mainPanel, "dashboard");
        });
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private static JPanel createTransferPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Transfer Money", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        JLabel receiverLabel = new JLabel("Receiver Account Number:");
        JTextField receiverField = new JTextField();
        
        JLabel amountLabel = new JLabel("Enter Amount:");
        JTextField amountField = new JTextField();
        
        JLabel pinLabel = new JLabel("Enter Security Pin:");
        JPasswordField pinField = new JPasswordField();
        
        JButton transferButton = new JButton("Transfer");
        JButton backButton = new JButton("Back");


        formPanel.add(receiverLabel);
        formPanel.add(receiverField);
        formPanel.add(amountLabel);
        formPanel.add(amountField);
        formPanel.add(pinLabel);
        formPanel.add(pinField);
        formPanel.add(backButton);
        formPanel.add(transferButton);
        
        transferButton.addActionListener(e -> {
            String receiverAccountText = receiverField.getText();
            String amountText = amountField.getText();
            String pin = new String(pinField.getPassword());
            
            if (receiverAccountText.isEmpty() || amountText.isEmpty() || pin.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, 
                        "All fields are required!", 
                        "Input Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            long receiverAccount;
            double amount;
            
            try {
                receiverAccount = Long.parseLong(receiverAccountText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainFrame, 
                        "Please enter a valid account number", 
                        "Input Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(mainFrame, 
                            "Amount must be positive", 
                            "Input Error", 
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainFrame, 
                        "Please enter a valid amount", 
                        "Input Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (receiverAccount == currentAccountNumber) {
                JOptionPane.showMessageDialog(mainFrame, 
                        "Cannot transfer to your own account", 
                        "Input Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                connection.setAutoCommit(false);
                
                // Verify sender account and PIN
                PreparedStatement senderCheck = connection.prepareStatement(
                        "SELECT * FROM Accounts WHERE account_number = ? AND security_pin = ?");
                senderCheck.setLong(1, currentAccountNumber);
                senderCheck.setString(2, pin);
                ResultSet senderResult = senderCheck.executeQuery();
                
                if (senderResult.next()) {
                    double currentBalance = senderResult.getDouble("balance");
                    
                    // Check if sufficient balance
                    if (amount <= currentBalance) {
                        // Verify receiver account exists
                        PreparedStatement receiverCheck = connection.prepareStatement(
                                "SELECT * FROM Accounts WHERE account_number = ?");
                        receiverCheck.setLong(1, receiverAccount);
                        ResultSet receiverResult = receiverCheck.executeQuery();
                        
                        if (receiverResult.next()) {
                            // Both accounts exist, proceed with transfer
                            String debitQuery = "UPDATE Accounts SET balance = balance - ? WHERE account_number = ?";
                            String creditQuery = "UPDATE Accounts SET balance = balance + ? WHERE account_number = ?";
                            
                            PreparedStatement debitStatement = connection.prepareStatement(debitQuery);
                            debitStatement.setDouble(1, amount);
                            debitStatement.setLong(2, currentAccountNumber);
                            
                            PreparedStatement creditStatement = connection.prepareStatement(creditQuery);
                            creditStatement.setDouble(1, amount);
                            creditStatement.setLong(2, receiverAccount);
                            
                            int debitRows = debitStatement.executeUpdate();
                            int creditRows = creditStatement.executeUpdate();
                            
                            if (debitRows > 0 && creditRows > 0) {
                                JOptionPane.showMessageDialog(mainFrame, 
                                        "Rs." + amount + " transferred successfully to account " + receiverAccount, 
                                        "Success", 
                                        JOptionPane.INFORMATION_MESSAGE);
                                connection.commit();
                                // Clear fields and return to dashboard
                                receiverField.setText("");
                                amountField.setText("");
                                pinField.setText("");
                                cardLayout.show(mainPanel, "dashboard");
                            } else {
                                JOptionPane.showMessageDialog(mainFrame, 
                                        "Transaction Failed!", 
                                        "Error", 
                                        JOptionPane.ERROR_MESSAGE);
                                connection.rollback();
                            }
                        } else {
                            JOptionPane.showMessageDialog(mainFrame, 
                                    "Receiver account does not exist!", 
                                    "Transaction Failed", 
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, 
                                "Insufficient Balance!", 
                                "Transaction Failed", 
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(mainFrame, 
                            "Invalid PIN!", 
                            "Authentication Failed", 
                            JOptionPane.ERROR_MESSAGE);
                }
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                try {
                    connection.rollback();
                    connection.setAutoCommit(true);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
                JOptionPane.showMessageDialog(mainFrame, 
                        "Database error: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        
        backButton.addActionListener(e -> {
            receiverField.setText("");
            amountField.setText("");
            pinField.setText("");
            cardLayout.show(mainPanel, "dashboard");
        });
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private static JPanel createBalancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Check Balance", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        JLabel pinLabel = new JLabel("Enter Security Pin:");
        JPasswordField pinField = new JPasswordField();
        
        JLabel balanceLabel = new JLabel("Current Balance:");
        JTextField balanceField = new JTextField();
        balanceField.setEditable(false);
        
        JButton checkButton = new JButton("Check Balance");
        JButton backButton = new JButton("Back");
        
        formPanel.add(pinLabel);
        formPanel.add(pinField);
        formPanel.add(balanceLabel);
        formPanel.add(balanceField);
        formPanel.add(backButton);
        formPanel.add(checkButton);
        
        checkButton.addActionListener(e -> {
            String pin = new String(pinField.getPassword());
            
            if (pin.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, 
                        "PIN is required!", 
                        "Input Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT balance FROM Accounts WHERE account_number = ? AND security_pin = ?");
                preparedStatement.setLong(1, currentAccountNumber);
                preparedStatement.setString(2, pin);
                ResultSet resultSet = preparedStatement.executeQuery();
                
                if (resultSet.next()) {
                    double balance = resultSet.getDouble("balance");
                    balanceField.setText("Rs. " + balance);
                } else {
                    JOptionPane.showMessageDialog(mainFrame, 
                            "Invalid PIN!", 
                            "Authentication Failed", 
                            JOptionPane.ERROR_MESSAGE);
                    balanceField.setText("");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(mainFrame, 
                        "Database error: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        
        backButton.addActionListener(e -> {
            pinField.setText("");
            balanceField.setText("");
            cardLayout.show(mainPanel, "dashboard");
        });
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Inner class to handle custom database operations for Account Manager functionality
    static class SwingAccountManager {
        private Connection connection;
        
        SwingAccountManager(Connection connection) {
            this.connection = connection;
        }
        
        public boolean debitMoney(long accountNumber, double amount, String securityPin) throws SQLException {
            connection.setAutoCommit(false);
            try {
                PreparedStatement checkStatement = connection.prepareStatement(
                        "SELECT * FROM Accounts WHERE account_number = ? and security_pin = ?");
                checkStatement.setLong(1, accountNumber);
                checkStatement.setString(2, securityPin);
                ResultSet resultSet = checkStatement.executeQuery();
                
                if (resultSet.next()) {
                    double currentBalance = resultSet.getDouble("balance");
                    if (amount <= currentBalance) {
                        String debitQuery = "UPDATE Accounts SET balance = balance - ? WHERE account_number = ?";
                        PreparedStatement debitStatement = connection.prepareStatement(debitQuery);
                        debitStatement.setDouble(1, amount);
                        debitStatement.setLong(2, accountNumber);
                        int rowsAffected = debitStatement.executeUpdate();
                        
                        if (rowsAffected > 0) {
                            connection.commit();
                            return true;
                        } else {
                            connection.rollback();
                            return false;
                        }
                    }
                }
                connection.rollback();
                return false;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
        
        public boolean creditMoney(long accountNumber, double amount, String securityPin) throws SQLException {
            connection.setAutoCommit(false);
            try {
                PreparedStatement checkStatement = connection.prepareStatement(
                        "SELECT * FROM Accounts WHERE account_number = ? and security_pin = ?");
                checkStatement.setLong(1, accountNumber);
                checkStatement.setString(2, securityPin);
                ResultSet resultSet = checkStatement.executeQuery();
                
                if (resultSet.next()) {
                    String creditQuery = "UPDATE Accounts SET balance = balance + ? WHERE account_number = ?";
                    PreparedStatement creditStatement = connection.prepareStatement(creditQuery);
                    creditStatement.setDouble(1, amount);
                    creditStatement.setLong(2, accountNumber);
                    int rowsAffected = creditStatement.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        connection.commit();
                        return true;
                    } else {
                        connection.rollback();
                        return false;
                    }
                }
                connection.rollback();
                return false;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
        
        public boolean transferMoney(long senderAccount, long receiverAccount, double amount, String securityPin) 
                throws SQLException {
            connection.setAutoCommit(false);
            try {
                // Verify sender account and PIN
                PreparedStatement senderCheck = connection.prepareStatement(
                        "SELECT * FROM Accounts WHERE account_number = ? AND security_pin = ?");
                senderCheck.setLong(1, senderAccount);
                senderCheck.setString(2, securityPin);
                ResultSet senderResult = senderCheck.executeQuery();
                
                if (senderResult.next()) {
                    double currentBalance = senderResult.getDouble("balance");
                    
                    // Check if sufficient balance
                    if (amount <= currentBalance) {
                        // Verify receiver account exists
                        PreparedStatement receiverCheck = connection.prepareStatement(
                                "SELECT * FROM Accounts WHERE account_number = ?");
                        receiverCheck.setLong(1, receiverAccount);
                        ResultSet receiverResult = receiverCheck.executeQuery();
                        
                        if (receiverResult.next()) {
                            // Both accounts exist, proceed with transfer
                            String debitQuery = "UPDATE Accounts SET balance = balance - ? WHERE account_number = ?";
                            String creditQuery = "UPDATE Accounts SET balance = balance + ? WHERE account_number = ?";
                            
                            PreparedStatement debitStatement = connection.prepareStatement(debitQuery);
                            debitStatement.setDouble(1, amount);
                            debitStatement.setLong(2, senderAccount);
                            
                            PreparedStatement creditStatement = connection.prepareStatement(creditQuery);
                            creditStatement.setDouble(1, amount);
                            creditStatement.setLong(2, receiverAccount);
                            
                            int debitRows = debitStatement.executeUpdate();
                            int creditRows = creditStatement.executeUpdate();
                            
                            if (debitRows > 0 && creditRows > 0) {
                                connection.commit();
                                return true;
                            }
                        }
                    }
                }
                connection.rollback();
                return false;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
        
        public Double getBalance(long accountNumber, String securityPin) throws SQLException {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT balance FROM Accounts WHERE account_number = ? AND security_pin = ?");
            statement.setLong(1, accountNumber);
            statement.setString(2, securityPin);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getDouble("balance");
            }
            return null;
        }
    }
}
