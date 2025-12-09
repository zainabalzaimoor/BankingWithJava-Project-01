package com.ga.project;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;

import static java.nio.file.Files.readAllLines;

public class FileService {
    private static final DateTimeFormatter LOG_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    static File myFile = new File("data.txt");
    private static final String TX_LOG_DIR = "transactions/"; // Directory for transaction logs
    private static final String DELIMITER = ";";

    private static void ensureTxDirExists() {
        Path dir = Paths.get(TX_LOG_DIR);
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            System.err.println("Failed to create transaction directory: " + e.getMessage());
        }
    }
    // idk
    private static DebitCard createCardInstanceByName(String name) {
        String cleanName = name.replace("Mastercard ", "").trim().toUpperCase();

        return switch (cleanName) {
            case "PLATINUM" -> new MastercardPlatinum();
            case "TITANIUM" -> new MastercardTitanium();
            // Handle cases where the card might just be saved as "Mastercard"
            case "MASTERCARD", "STANDARD" -> new Mastercard();
            default -> null;
        };
    }

    public List<User> readAllUsers() {
        List<User> allUsers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(myFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    String[] parts = line.split(DELIMITER);

                    if (parts.length < 4) {
                        System.err.println("Skipping malformed line (too few fields before Role): " + line);
                        continue;
                    }

                    String id = parts[0];
                    String username = parts[1];
                    String password = parts[2];
                    String role = parts[3];

                    User user;

                    if (role.equalsIgnoreCase("Banker")) {
                        user = new Admin(id, username, password, role);

                    } else if (role.equalsIgnoreCase("Customer")) {

                        // EXPECT 7 FIELDS (ID, User, Pass, Role, Accounts, Balances, CardTypes)
                        if (parts.length != 7) {
                            System.err.println("Skipping malformed Customer line (expected 7 fields, got " + parts.length + "): " + line);
                            continue;
                        }

                        String accountsPart = parts[4].replaceAll("[\\[\\]\\s]", "");
                        String[] accountNumbers = accountsPart.split(",");

                        String balancesPart = parts[5].replaceAll("[\\[\\]\\s]", "");
                        String[] balancesStr = balancesPart.split(",");

                        // NEW: Parse card types (parts[6])
                        String cardsPart = parts[6].replaceAll("[\\[\\]\\s]", "");
                        String[] cardNames = cardsPart.split(",");


                        if (accountNumbers.length != balancesStr.length || accountNumbers.length != cardNames.length) { // <-- CHECK ADDED
                            System.err.println("Skipping malformed Customer line: Account/Balance/Card count mismatch. Line: " + line);
                            continue;
                        }

                        List<Account> accounts = new ArrayList<>();
                        for (int i = 0; i < accountNumbers.length; i++) {
                            String accNum = accountNumbers[i].trim();
                            double balance;
                            try {
                                balance = Double.parseDouble(balancesStr[i].trim());
                            } catch (NumberFormatException e) {
                                System.err.println("Failed to parse balance for account: " + accNum);
                                throw new RuntimeException("Data format error encountered.", e);
                            }

                            String cardName = cardNames[i].trim();
                            DebitCard linkedCard = createCardInstanceByName(cardName);
                            if (linkedCard == null) {
                                System.err.println("Unknown card type: " + cardName + ". Defaulting to Mastercard.");
                                linkedCard = new Mastercard();
                            }

                            Account account;
                            if (accNum.contains("BA")) {
                                account = new BankingAccount(accNum, balance, linkedCard);
                            } else if (accNum.contains("SA")) {
                                account = new SavingAccount(accNum, balance, linkedCard);
                            } else {
                                throw new IllegalArgumentException("Unknown account type: " + accNum);
                            }

                            accounts.add(account);
                        }

                        user = new Customer(id, username, password, role, accounts);

                    } else {
                        System.err.println("Skipping line with unknown role (" + role + "): " + line);
                        continue;
                    }

                    allUsers.add(user);

                } catch (Exception e) {
                    System.err.println("Fatal error during user parsing: " + e.getMessage());
                    e.printStackTrace();
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading all users: " + e.getMessage());
        }

        return allUsers;
    }
    public void saveToFile(Customer customer) {
        ensureTxDirExists();

        String txFileName = TX_LOG_DIR + "Customer-" + customer.getUser_name() + "-" + customer.getUser_id() + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(myFile, true))) {

            List<String> accounts = customer.getUserAccounts().stream()
                    .map(Account::getAccount_number).toList();

            List<Double> balances = customer.getUserAccounts().stream()
                    .map(Account::getBalance).toList();

            List<String> cardTypes = customer.getUserAccounts().stream()
                    .map(Account::getLinkedCardName).toList();

            writer.write(customer.getUser_id() + DELIMITER +
                    customer.getUser_name() + DELIMITER +
                    customer.getPassword() + DELIMITER +
                    customer.getRole() + DELIMITER +
                    accounts + DELIMITER +
                    balances+ DELIMITER +
                    cardTypes);

            writer.newLine();
            System.out.println("Data written to " + myFile);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }

        //  Create the customer's empty transaction file by default
        try {
            Path txFilePath = Paths.get(txFileName);
            if (!Files.exists(txFilePath)) {
                Files.createFile(txFilePath);
                System.out.println("Created default transaction file: " + txFileName);
            }
        } catch (IOException e) {
            System.err.println("Error creating default transaction file for " + customer.getUser_name() + ": " + e.getMessage());
        }
    }

    public void updatePassword(String username, String newEncryptedPassword) {
        try {
            List<String> lines = readAllLines(Paths.get("data.txt"));

            for (int i = 0; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(";");

                if (parts[1].equals(username)) {
                    parts[2] = newEncryptedPassword;

                    // Rebuild the line
                    lines.set(i, String.join(";", parts));
                    break;
                }
            }

            Files.write(Paths.get("data.txt"), lines);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateCustomerRecord(Customer customer) {
        try {
            List<String> lines = Files.readAllLines(Path.of("data.txt"));

            List<Account> accounts = customer.getUserAccounts();

            String newAccountNumbers = accounts.stream()
                    .map(Account::getAccount_number)
                    .collect(Collectors.joining(", "));

            String newBalances = accounts.stream()
                    .map(Account::getBalance)
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));

            String newAccountsPart = "[" + newAccountNumbers + "]";
            String newBalancesPart = "[" + newBalances + "]";

            for (int i = 0; i < lines.size(); i++) {
                String currentLine = lines.get(i);
                String[] parts = currentLine.split(DELIMITER);

                if (parts.length > 1 && parts[1].equals(customer.getUser_name())) {

                    if (parts.length < 6 || !parts[3].equalsIgnoreCase("Customer")) {
                        continue;
                    }

                    parts[4] = newAccountsPart;
                    parts[5] = newBalancesPart;

                    lines.set(i, String.join(DELIMITER, parts));
                    break;
                }
            }

            Files.write(Path.of("data.txt"), lines);

        } catch (IOException e) {
            System.err.println("Error updating customer record for user " + customer.getUser_name() + ": " + e.getMessage());
        }
    }

    public User findByUsername(String username){
        List<User> current_users = readAllUsers();
        for (User customer: current_users){
            if(customer.getUser_name().equalsIgnoreCase(username))
                return customer;
        }
        return null;
    }

    public Account findAccountByNumber(String accountNumber) {
        List<User> allUsers = readAllUsers();

        for (User user : allUsers) {
            if (user instanceof Customer customer) {
                for (Account account : customer.getUserAccounts()) {
                    if (account.getAccount_number().equalsIgnoreCase(accountNumber)) {
                        return account;
                    }
                }
            }
        }
        return null;
    }

    public Customer findCustomerByAccount(Account targetAccount) {
        List<User> allUsers = readAllUsers();

        for (User user : allUsers) {
            if (user instanceof Customer customer) {
                boolean ownsAccount = customer.getUserAccounts().stream()
                        .anyMatch(acc -> acc.getAccount_number()
                                .equals(targetAccount.getAccount_number()));

                if (ownsAccount) {
                    return customer;
                }
            }
        }
        return null;
    }

    public void appendTransaction(Customer user, String transactionDetails) {
        ensureTxDirExists();
        String txFileName = TX_LOG_DIR + "Customer-" + user.getUser_name() + "-"+ user.getUser_id() + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(txFileName, true))) {
            writer.write(transactionDetails);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing transaction log for user " + user.getUser_id() + ": " + e.getMessage());
        }
    }

    public List<String> readTransactions(Customer user) {
        String txFileName = TX_LOG_DIR + "Customer-" + user.getUser_name() + "-"+ user.getUser_id() + ".txt";
        List<String> transactions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(txFileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                transactions.add(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("No transaction history found for user " + user.getUser_name() + " (File exists but may be empty).");
        } catch (IOException e) {
            System.err.println("Error reading transaction log: " + e.getMessage());
        }
        return transactions;
    }

    public List<TransactionRecord> parseTransactions(Customer customer) {
        List<String> transactionsLines = readTransactions(customer);

        String fullContent = String.join("\n", transactionsLines);

        List<TransactionRecord> records = new ArrayList<>();

        String[] blocks = fullContent.split("\n#####\n");

        for (String block : blocks) {
            String trimmedBlock = block.trim();
            if (trimmedBlock.isEmpty()) continue;

            TransactionRecord record = new TransactionRecord();
            String[] lines = trimmedBlock.split("\n");

            try {
                for (String line : lines) {
                    if (line.startsWith("DATE: ")) {
                        record.dateTime = line.substring("DATE: ".length()).trim();
                        record.parsedDateTime = LocalDateTime.parse(record.dateTime, LOG_DATE_FORMATTER);
                    } else if (line.startsWith("TYPE: ")) {
                        record.type = line.substring("TYPE: ".length()).trim();
                    } else if (line.startsWith("ACCOUNT: ")) {
                        record.account = line.substring("ACCOUNT: ".length()).trim();
                    } else if (line.startsWith("AMOUNT: ")) {
                        String amountStr = line.substring("AMOUNT: ".length()).trim();
                        record.amount = Double.parseDouble(amountStr);
                    } else if (line.startsWith("NEW_BALANCE: ")) {
                        record.newBalance = Double.parseDouble(line.substring("NEW_BALANCE: ".length()).trim());
                    } else if (line.startsWith("NOTES: ")) {
                        record.notes = line.substring("NOTES: ".length()).trim();
                    } else if (line.startsWith("TO_ACCOUNT: ")) {
                        record.notes = "Transfer To: " + line.substring("TO_ACCOUNT: ".length()).trim();
                    } else if (line.startsWith("FROM_ACCOUNT: ")) {
                        record.notes = "Transfer From: " + line.substring("FROM_ACCOUNT: ".length()).trim();
                    }
                }

                if (record.notes == null || record.notes.isEmpty() || record.notes.equalsIgnoreCase("N/A")) {
                    record.notes = "N/A";
                }

                records.add(record);

            } catch (Exception e) {
                System.err.println("Error parsing transaction block: " + trimmedBlock + ". Skipping this record.");
                e.printStackTrace();
            }
        }
        return records;
    }
    public List<TransactionRecord> filterTransactions(Customer customer, String filterType) {

        List<TransactionRecord> allRecords = parseTransactions(customer);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime filterStart = null;
        LocalDateTime filterEnd = now;


        switch (filterType.toLowerCase()) {
            case "today" -> filterStart = now.truncatedTo(ChronoUnit.DAYS);
            case "yesterday" -> {
                filterStart = now.minusDays(1).truncatedTo(ChronoUnit.DAYS);
                filterEnd = now.truncatedTo(ChronoUnit.DAYS);
            }
            case "last week", "last 7 days" -> filterStart = now.minusDays(7).truncatedTo(ChronoUnit.DAYS);
            case "last month", "last 30 days" -> filterStart = now.minusDays(30).truncatedTo(ChronoUnit.DAYS);
            case "all" -> {
                return allRecords;
            }
            default -> {
                System.err.println("Invalid filter type specified: " + filterType);
                return new ArrayList<>();
            }
        }

        final LocalDateTime finalFilterStart = filterStart;
        final LocalDateTime finalFilterEnd = filterEnd;

        return allRecords.stream()
                .filter(record -> record.parsedDateTime != null &&
                        !record.parsedDateTime.isBefore(finalFilterStart) &&
                        record.parsedDateTime.isBefore(finalFilterEnd))
                .collect(Collectors.toList());
    }

    public double calculateTodayUsage(Customer customer, String accountNumber, String transactionType) {
        // Filter all records to get today records
        List<TransactionRecord> todayRecords = filterTransactions(customer, "today");
        double totalUsage = 0.0;

        for (TransactionRecord record : todayRecords) {
            if (record.account.equals(accountNumber) &&
                    record.type.equalsIgnoreCase(transactionType)) {

                totalUsage += Math.abs(record.amount);
            }
        }
        return totalUsage;
    }

}