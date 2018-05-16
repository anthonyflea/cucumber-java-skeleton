package pl.edu.agh.iet.katabank;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Deposit implements BankProduct {

    private static final int DEFAULT_DURATION = 12;

    private BigDecimal balance;
    private Account connectedAccount;
    private final UUID id;
    private final String INCORRECT_AMOUNT_MESSAGE = "Incorrect initial balance to open deposit: ";
    private final LocalDate openDate;
    private final int durationInMonths;
    private boolean open;

    public Deposit(Account account, BigDecimal initialBalance) {
        this(account, initialBalance, LocalDate.now(), DEFAULT_DURATION);
    }

    public Deposit(Account account, BigDecimal initialBalance, LocalDate openDate, int durationInMonths) {
        try {
            account.withdraw(initialBalance);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(INCORRECT_AMOUNT_MESSAGE + initialBalance, e);
        }

        this.balance = initialBalance;
        this.connectedAccount = account;
        this.id = UUID.randomUUID();
        this.openDate = openDate;
        this.durationInMonths = durationInMonths;
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

    public void finishDeposit(LocalDate date) {
        if (date == null || date.isBefore(this.openDate.plusMonths(durationInMonths))) {
            throw new RuntimeException("Cannot close deposit on date: " + date);
        }
        BigDecimal closeBalance = this.balance;
        this.balance = BigDecimal.ZERO;
        this.connectedAccount.deposit(closeBalance);
        this.open = false;
    }
}
