package com.ga.project;
import java.util.List;
import java.util.Scanner;

import java.util.concurrent.TimeUnit;

public class AuthService {
//    private static boolean isLogin = false;
    Scanner scanner = new Scanner(System.in);
    private static int fail_attempts = 0;
    private static final long LOCK_DURATION_MS = TimeUnit.MINUTES.toMillis(1);
    private static long lockStartTime = 0;
    private User logged_in_user = null;

    FileService fileService = new FileService();


    // the admin will create the users and assign them account by default in this step as well and initally the balance
    // will be = 0 so also in the file we should save the account number there assigned for each customer so that we'd know the user has this account

    // Signup called by admin
    public void signup(Customer customer){
        if(fileService.findByUsername(customer.getUser_name()) != null){
            System.out.println("Username already exist.");
            return;
        }
        if(customer.getUser_id() == null || customer.getUser_name() == null || customer.getPassword() == null){
            System.out.println("All fields are required!");
            return;
        }

        try {
//            String encrypted_password = PasswordEncryption.hashPassword(customer.getPassword());
//            customer.setPassword(encrypted_password);
            customer.setUser_id(customer.getUser_id());
            customer.setUser_name(customer.getUser_name());
            customer.setRole("Customer");
            fileService.saveToFile(customer);
            System.out.println("Customer created successfully!");

        }
        catch (Exception e){
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }

    }

//    public void signup(Customer customer) {
//        if (fileService.findByUsername(customer.getUser_name()) != null) {
//            System.out.println("Username already exists!");
//            return;
//        }
//        String hashed = PasswordEncryption.hashPassword(customer.getPassword());
//        customer.setPassword(hashed);
//        FileService.saveToFile(customer);
//        System.out.println("Customer created successfully!");
//    }
//

    public String login(String username,String password){
        User user = fileService.findByUsername(username);
        if (user == null) {
            System.out.println("User does not exist!");
            return "USER_NOT_FOUND";
        }
        if(user.getPassword().equals("")){
            return "EMPTY_PASSWORD";
        }

        long now = System.currentTimeMillis();

        // Check if user is currently locked
        if (fail_attempts >= 3) {
            if (now - lockStartTime < LOCK_DURATION_MS) {
                System.out.println("Too many failed attempts. Please wait 1 minute.");
                return "WARRING: WAIT_ONE_MINUTE";
            } else {
                // Lock expired → reset attempts
                fail_attempts = 0;
            }
    }

    String hashed = PasswordEncryption.hashPassword(password);

        assert hashed != null;
        if (!hashed.equals(user.getPassword())) {
        fail_attempts++;

        if (fail_attempts == 3) {
            // Start lock timer
            lockStartTime = now;
        }

        System.out.println("Incorrect password! You " + (3-fail_attempts) + " Attempts left.");
        return "WRONG_PASSWORD";
    }

    // Successful login
//        if ("Banker".equals(user.getRole())) {
//            // make sure we store an Admin object
//            logged_in_user = new Admin(user.getUser_id(), user.getUser_name(), user.getPassword(), "Banker");
//        } else {
//            logged_in_user = user;
////            Customer c = fileService.findById(user.getUser_id());
////            logged_in_user = new Customer(c.getUser_id(), c.getUser_name(), c.getPassword(), "Customer", c.getUserAccounts());
//        }
    logged_in_user = user;
    fail_attempts = 0;
    System.out.println("Login successful! Welcome " + username);
    return "SUCCESS";
    }

    public void setPassword(String username, String password){
        User user = fileService.findByUsername(username);
        if (user == null)
            System.out.println("User does not exist!");
        String encrypted_password = PasswordEncryption.hashPassword(password);
        assert user != null;
        fileService.updatePassword(user.getUser_name(),encrypted_password);
    }

    public void logout(){
        if (logged_in_user != null) {
            System.out.println(logged_in_user.getRole() + " " +logged_in_user.getUser_name() + " logged out.");
            logged_in_user = null;
        }
        else System.out.println("No user is currently logged in.");
    }

    public User getLoggedInUser() {
        return logged_in_user;
    }

}
