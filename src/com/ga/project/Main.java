////package com.ga.project;
////import java.util.Scanner;
////public class Main {
////    public static void main(String[] args) {
////        Scanner scanner = new Scanner(System.in);
////        System.out.println("Welcome to ACME Bank :) ");
//////
//////        System.out.println("please signup first!");
//////        System.out.println("Enter user id: ");
//////        String id = scanner.nextLine();
//////        System.out.println("Enter a user name: ");
//////        String name = scanner.nextLine();
//////        System.out.println("Enter a password: ");
//////        String password = scanner.nextLine();
//////
////////        Customer newCustomer = new Customer(id,name,password,"Customer");
//////        Admin.createCustomer(id,name,password);
//////
//////        System.out.println("please login first!");
//////        System.out.println("Enter your username: ");
//////        String name = scanner.nextLine().trim();
//////        System.out.println("Enter your password: ");
//////        String password = scanner.nextLine().trim();
//////
//////
//////        AuthService.login(name,password);
//////
//////        while (true) {
//////            System.out.println("===== Login =====");
//////            System.out.print("Enter username: ");
//////            String username = scanner.nextLine();
//////
//////            System.out.print("Enter password: ");
//////            String password = scanner.nextLine();
//////
//////            AuthService authService = new AuthService();
//////
//////            authService.login(username, password);
//////            Customer customer = authService.getLoggedInUser();
//////
//////            if (customer != null) {
//////                System.out.println("Login successful. Welcome " + customer.getUser_name() + "!");
//////                break; // exit loop and continue program
//////            }
//////        }
////
////
////
////
////
////    }
////}
//
//
//package com.ga.project;
//
//import java.util.Scanner;
//
//public class Main {
//
//    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);
//
//// Initialize admin/banker (already exists)
//        Admin admin = new Admin("ADM-001", "banker", "bank123", "Banker");
//
//        AuthService authService = new AuthService();
//
//        while (true) {
//            System.out.println("\n==== Welcome to Bank System ====");
//            System.out.println("1. Admin Login");
//            System.out.println("2. Customer Login");
//            System.out.println("3. Exit");
//            System.out.print("Choose an option: ");
//            String option = scanner.nextLine();
//
//            switch (option) {
//                case "1":
//                    System.out.print("Enter admin username: ");
//                    String adminUser = scanner.nextLine();
//                    System.out.print("Enter admin password: ");
//                    String adminPass = scanner.nextLine();
//
//                    if (adminUser.equals(admin.getUser_name()) && adminPass.equals(admin.getPassword())) {
//                        System.out.println("Admin logged in successfully!");
//                        adminMenu(scanner, admin);
//                    } else {
//                        System.out.println("Invalid admin credentials!");
//                    }
//                    break;
//
//                case "2":
//                    System.out.print("Enter username: ");
//                    String username = scanner.nextLine();
//                    System.out.print("Enter password: ");
//                    String password = scanner.nextLine();
//
//                     authService.login(username, password);
//                    String role = authService.getLoggedInUser().getRole();
//
//                    if (role != null && role.equalsIgnoreCase("Customer")) {
//                        User loggedUser = authService.getLoggedInUser();
//
//// Force password setup if empty
//                        if (loggedUser.getPassword().isEmpty()) {
//                            System.out.print("Set your new password: ");
//                            String newPass = scanner.nextLine();
//                            loggedUser.setPassword(newPass);
//                            System.out.println("Password set successfully!");
//                        }
//
//// Simple customer menu
//                        customerMenu(scanner, authService);
//                    }
//                    break;
//
//                case "3":
//                    System.out.println("Exiting system. Goodbye!");
//                    System.exit(0);
//                    break;
//
//                default:
//                    System.out.println("Invalid option!");
//            }
//        }
//    }
//
//    // --- Admin Menu ---
//    private static void adminMenu(Scanner scanner, Admin admin) {
//        while (true) {
//            System.out.println("\n--- Admin Menu ---");
//            System.out.println("1. Create Customer");
//            System.out.println("2. Logout");
//            System.out.print("Choose an option: ");
//            String choice = scanner.nextLine();
//
//            switch (choice) {
//                case "1":
//                    System.out.print("Enter Customer ID: ");
//                    String userId = scanner.nextLine();
//                    System.out.print("Enter Customer username: ");
//                    String username = scanner.nextLine();
//                    admin.createCustomer(userId, username);
//                    break;
//
//                case "2":
//                    System.out.println("Admin logged out.");
//                    return;
//
//                default:
//                    System.out.println("Invalid option!");
//            }
//        }
//    }
//
//    // --- Customer Menu ---
//    private static void customerMenu(Scanner scanner, AuthService authService) {
//        while (true) {
//            User user = authService.getLoggedInUser();
//            System.out.println("\n--- Customer Menu ---");
//            System.out.println("Welcome " + user.getUser_name());
//            System.out.println("1. View Accounts");
//            System.out.println("2. Logout");
//            System.out.print("Choose an option: ");
//            String choice = scanner.nextLine();
//
//            switch (choice) {
//                case "1":
//                    System.out.println("--- Accounts ---");
//                    if (user.getUserAccounts().isEmpty()) {
//                        System.out.println("No accounts assigned yet.");
//                    } else {
//                        user.getUserAccounts().forEach(acc ->
//                                System.out.println(acc.getAccount_number() + " | Balance: " + acc.getBalance()));
//                    }
//                    break;
//
//                case "2":
//                    authService.logout();
//                    System.out.println("Logged out successfully!");
//                    return;
//
//                default:
//                    System.out.println("Invalid option!");
//            }
//        }
//    }
//}