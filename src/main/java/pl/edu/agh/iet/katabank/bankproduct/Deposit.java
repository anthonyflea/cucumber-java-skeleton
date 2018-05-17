package pl.edu.agh.iet.katabank.bankproduct;

import pl.edu.agh.iet.katabank.Customer;
import pl.edu.agh.iet.katabank.bankproduct.deposittype.DepositType;
import pl.edu.agh.iet.katabank.bankproduct.deposittype.MonthlyDepositType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Deposit implements BankProduct {

    private static final int DEFAULT_DURATION = 12;
    private static final String INCORRECT_INITIAL_BALANCE_MESSAGE = "Incorrect initial balance to open deposit: ";
    private static final String CANNOT_CLOSE_ALREADY_CLOSED_DEPOSIT_MESSAGE = "Cannot close already closed deposit";
    private static final String CANNOT_CLOSE_DEPOSIT_ON_DATE_MESSAGE = "Cannot close deposit on date: ";

    private final BigDecimal interestRate;

    private BigDecimal balance;
    private Account connectedAccount;
    private final UUID id;
    private final LocalDate openDate;
    private final int durationInMonths;
    private boolean open;

    public Deposit(Account account, BigDecimal initialBalance) {
        this(account, initialBalance, LocalDate.now(), DEFAULT_DURATION);
    }

    public Deposit(Account account, BigDecimal initialBalance, LocalDate openDate, int durationInMonths) {
        this(account, initialBalance, openDate, new MonthlyDepositType(durationInMonths, BigDecimal.ZERO));
    }

    public Deposit(Account account, BigDecimal initialBalance, LocalDate openDate, DepositType depositType) {
        try {
            account.withdraw(initialBalance);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(INCORRECT_INITIAL_BALANCE_MESSAGE + initialBalance, e);
        }

        this.balance = initialBalance;
        this.connectedAccount = account;
        this.id = UUID.randomUUID();
        this.openDate = openDate;
        this.durationInMonths = depositType.getDuration();
        this.interestRate = depositType.getYearlyInterestRate();
        this.open = true;
    }

    public Account getConnectedAccount() {
        return this.connectedAccount;
    }

    public BigDecimal getBalance() {
        return this.balance;
    }

    public LocalDate getOpenDate() {
        return this.openDate;
    }

    public boolean isOpen() {
        return this.open;
    }

    @Override
    public Customer getOwner() {
        return this.connectedAccount.getOwner();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Deposit deposit = (Deposit) o;
        return Objects.equals(id, deposit.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void closeDeposit(LocalDate date) {
        if (!isOpen()) {
            throw new RuntimeException(CANNOT_CLOSE_ALREADY_CLOSED_DEPOSIT_MESSAGE);
        }
        if (date == null || date.isBefore(this.openDate.plusMonths(durationInMonths))) {
            throw new RuntimeException(CANNOT_CLOSE_DEPOSIT_ON_DATE_MESSAGE + date);
        }
        BigDecimal closeBalance = this.balance;
        this.balance = BigDecimal.ZERO;
        BigDecimal balanceWithInterest
                = closeBalance.add(closeBalance.multiply(interestRate.divide(new BigDecimal(100)).multiply(new BigDecimal(durationInMonths).divide(new BigDecimal(12)))));
        this.connectedAccount.deposit(balanceWithInterest);
        this.open = false;
    }

    public int getDuration() {
        return durationInMonths;
    }
}
