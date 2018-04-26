package pl.edu.agh.iet.katabank;

import java.math.BigDecimal;
import java.util.Set;

public class Bank {

    private AccountsRepository accountsRepository;

    public Bank(AccountsRepository accountsRepository) {
        this.accountsRepository = accountsRepository;
    }

    public Set<Account> getAccountsForCustomer(Customer customer) {
        return accountsRepository.findAccountsForCustomer(customer);
    }

    public void deposit(Customer customer, Account account, BigDecimal depositAmount) {
        checkOperationNotAllowed(customer, account, "Customer cannot deposit money to others account.");
        account.deposit(depositAmount);
    }

    public void withdraw(Customer customer, Account account, BigDecimal withdrawAmount) {
        checkOperationNotAllowed(customer, account, "Customer cannot withdraw money from others account.");
        account.withdraw(withdrawAmount);
    }

    public void transfer(Customer customer, Account customersAccount, Account targetAccount, BigDecimal transferAmount) {
        checkOperationNotAllowed(customer, customersAccount, "Customer cannot transfer money from others account.");
        customersAccount.transfer(targetAccount, transferAmount);
    }

    private void checkOperationNotAllowed(Customer customer, Account account, String message) {
        if (!getAccountsForCustomer(customer).contains(account)) {
            throw new RuntimeException(message);
        }
    }
}
