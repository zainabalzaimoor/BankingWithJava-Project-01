package com.ga.project;
import java.util.concurrent.TimeUnit;

public class AuthService {
    private static int fail_attempts = 0;
    private static final long LOCK_DURATION_MS = TimeUnit.MINUTES.toMillis(1);
    private static long lockStartTime = 0;
    private User logged_in_user = null;
    FileService fileService = new FileService();
    PasswordEncryption passwordEncryption = new PasswordEncryption();

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

    public String login(String username, String password) {

        User user = fileService.findByUsername(username);

        // Account lock check
        long now = System.currentTimeMillis();

        if (fail_attempts >= 3) {
            if (now - lockStartTime < LOCK_DURATION_MS) {
                return "LOCKED_ACCOUNT";
            } else {
                fail_attempts = 0; // lock expired
            }
        }

        // Check password
        boolean isEqual = passwordEncryption.verifyPassword(password, user.getPassword());

        if (!isEqual) {
            fail_attempts++;
            if (fail_attempts == 3) lockStartTime = now;
            return "WRONG_PASSWORD";
        }

        // Success
        logged_in_user = user;
        fail_attempts = 0;
        return "SUCCESS";
    }

    public void setPassword(String username, String password){
        User user = fileService.findByUsername(username);
        if (user == null)
            System.out.println("User does not exist!");
        String encrypted_password = passwordEncryption.hashPassword(password);
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

    public boolean userHasNoPassword(String name){
        for (User user : fileService.readAllUsers()) {
            if(user.getUser_name().equals(name)) {
            if (user.getPassword() == null || user.getPassword().isEmpty() || user.getPassword().equals("DEFAULT_UNSET_PASS")) {
                return true;
            }
            }
        }
        return false;

    }
    public boolean checkUsers(String username){
    User user = fileService.findByUsername(username);
    return user != null;
    }

    public int getAttemptsLeft() {
        return Math.max(0, 3 - fail_attempts);
    }



}
