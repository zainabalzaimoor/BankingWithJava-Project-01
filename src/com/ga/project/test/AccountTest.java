package com.ga.project.test;

import com.ga.project.Account;
import com.ga.project.DebitCard;
import com.ga.project.Mastercard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    // Concrete subclass for testing
    static class TestAccount extends Account {
        public TestAccount(String account_number, String account_type, DebitCard linkedCard) {
            super(account_number, account_type, linkedCard);
        }

        public TestAccount(String account_number, String account_type, double balance, DebitCard linkedCard) {
            super(account_number, account_type, balance, linkedCard);
        }
    }

    private Account acc1;
    private Account acc2;
    private DebitCard dummyCard;

    @BeforeEach
    void setUp() {
        dummyCard = new Mastercard();
        acc1 = new TestAccount("ACC001", "Checking", dummyCard);
        acc2 = new TestAccount("ACC002", "Savings", 500, dummyCard);
    }

    @Test
    void testDepositPositiveAmount() {
        acc1.deposit(200);
        assertEquals(200, acc1.getBalance());
    }

    @Test
    void testDepositNegativeAmount() {
        acc1.deposit(-100);
        assertEquals(0, acc1.getBalance()); // Balance should not change
    }

    @Test
    void testWithdrawPositiveAmount() {
        acc2.withdraw(100);
        assertEquals(400, acc2.getBalance());
    }

    @Test
    void testWithdrawMoreThanBalanceWithOverdraft() {
        // acc1 has balance 0
        acc1.withdraw(50);
        assertTrue(acc1.getBalance() < 0); // Overdraft fee applied
    }

    @Test
    void testWithdrawNegativeOrZeroAmount() {
        acc2.withdraw(-50);
        assertEquals(500, acc2.getBalance()); // Balance should not change

        acc2.withdraw(0);
        assertEquals(500, acc2.getBalance()); // Balance should not change
    }

    @Test
    void testTransferMoneyValid() {
        acc1.deposit(300);
        acc1.transferMoney(acc1, acc2, 100);
        assertEquals(200, acc1.getBalance());
        assertEquals(600, acc2.getBalance());
    }

    @Test
    void testTransferMoneyNegativeAmount() {
        acc1.deposit(300);
        acc1.transferMoney(acc1, acc2, -50);
        assertEquals(300, acc1.getBalance());
        assertEquals(500, acc2.getBalance());
    }

    @Test
    void testTransferMoneyToSameAccount() {
        acc1.deposit(200);
        acc1.transferMoney(acc1, acc1, 100);
        assertEquals(200, acc1.getBalance()); // Balance should not change
    }
}
