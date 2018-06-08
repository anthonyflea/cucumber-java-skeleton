package pl.edu.agh.iet.katabank.steps;

import cucumber.api.java8.En;
import org.assertj.core.data.Offset;
import pl.edu.agh.iet.katabank.Bank;
import pl.edu.agh.iet.katabank.Customer;
import pl.edu.agh.iet.katabank.bankproduct.Account;
import pl.edu.agh.iet.katabank.bankproduct.Deposit;
import pl.edu.agh.iet.katabank.bankproduct.amount.DepositPayment;
import pl.edu.agh.iet.katabank.bankproduct.amount.Payment;
import pl.edu.agh.iet.katabank.bankproduct.interestpolicy.DepositDurationDetails;
import pl.edu.agh.iet.katabank.bankproduct.interestpolicy.DepositDurationDetails.DurationType;
import pl.edu.agh.iet.katabank.bankproduct.interestpolicy.InterestPolicy;
import pl.edu.agh.iet.katabank.bankproduct.interestpolicy.MonthlyInterestPolicy;
import pl.edu.agh.iet.katabank.repository.BankProductsRepository;
import pl.edu.agh.iet.katabank.repository.InMemoryBankProductsRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DepositManagementSteps implements En {

    private BankProductsRepository bankProductsRepository;
    private Customer customer;
    private Bank bank;
    private Account account;
    private Deposit deposit;
    private Set<Deposit> customerDeposits;
    private LocalDate date;
    private BigDecimal amount;
    private InterestPolicy interestPolicy = new MonthlyInterestPolicy(new BigDecimal(10));
    DepositDurationDetails durationDetails = new DepositDurationDetails(12, DurationType.MONTHS);

    public DepositManagementSteps() {

        Given("^there is a bank with account and customer$", () -> {
            bankProductsRepository = new InMemoryBankProductsRepository();
            bank = new Bank(bankProductsRepository);
            customer = new Customer();
        });

        Given("^there is a default deposit type$", ()
                -> interestPolicy = new MonthlyInterestPolicy(new BigDecimal(12)));

        Given("^a customer has an account with balance (\\d+)$", (Integer balance) -> {
            account = new Account(customer);
            account.setBalance(new BigDecimal(balance));
            bankProductsRepository.addAccount(account);
        });

        When("^he opens a deposit with balance (\\d+)$", (Integer depositBalance) -> {
            DepositDurationDetails durationDetails = new DepositDurationDetails(12, DurationType.MONTHS);
            deposit = bank.openDeposit(customer, account, new BigDecimal(depositBalance), durationDetails, interestPolicy);
            bankProductsRepository.addDeposit(deposit);
        });

        Then("^he owns a deposit with balance (\\d+)$", (Integer depositBalance) -> {
            customerDeposits = bank.getDepositsForCustomer(customer);
            assertThat(customerDeposits).contains(deposit);
            assertThat(deposit.getBalance()).isEqualByComparingTo(new BigDecimal(depositBalance));
        });

        And("^the account has balance (\\d+)$", (Integer accountNewBalance)
                -> assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal(accountNewBalance)));

        Given("^a customer opened a deposit for a period of one year$", () -> {
            account = new Account(customer);
            bankProductsRepository.addAccount(account);
            amount = new BigDecimal("10");
            account.setBalance(amount);
            date = LocalDate.now();
            deposit = bank.openDeposit(customer, account, amount, durationDetails, interestPolicy);
        });

        When("^one year has passed$", () -> {
            assertThat(account.getBalance()).isLessThan(amount);
            assertThat(deposit.getBalance()).isEqualByComparingTo(amount);
            date = date.plusMonths(12);
        });

        Then("^the money is transferred back to the account the funds were taken from$", () -> {
            deposit.closeDeposit(date);
            assertThat(account.getBalance()).isGreaterThanOrEqualTo(amount);
            assertThat(deposit.getBalance()).isZero();
            assertThat(deposit.isOpen()).isFalse();
        });

        Given("^bank offers a deposit for a period of (\\d+) months with yearly interest rate (\\d+)%$",
                (Integer durationInMonths, Integer interestRate) -> {
                    interestPolicy = new MonthlyInterestPolicy(new BigDecimal(interestRate));
                    durationDetails = new DepositDurationDetails(durationInMonths, DurationType.MONTHS);

                });

        And("^customer opens that deposit with funds (\\d+)$", (Integer initialBalance) -> {
            amount = new BigDecimal(initialBalance);
            account = new Account(customer);
            bankProductsRepository.addAccount(account);
            account.setBalance(amount);
            date = LocalDate.now();
            deposit = bank.openDeposit(customer, account, amount, durationDetails, interestPolicy);
        });

        When("^a termination date has passed$", () -> {
            date = deposit.getCloseDate();
            deposit.closeDeposit(date);
        });

        Then("^(\\d+) is transferred back to his account$",
                (Integer newBalance) ->
                        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal(newBalance)));

        Given("^there is a customer with a deposit opened$", () -> {
            account = new Account(customer);
            account.setBalance(new BigDecimal(100));
            bankProductsRepository.addAccount(account);
            DepositDurationDetails depositDurationDetails = new DepositDurationDetails(6, DurationType.MONTHS);
            InterestPolicy interestPolicy = new MonthlyInterestPolicy(new BigDecimal(10));
            deposit = bank.openDeposit(customer, account, new BigDecimal(100), depositDurationDetails, interestPolicy);
            bankProductsRepository.addDeposit(deposit);
        });

        When("^he transfers new funds to the existing deposit$", () -> {
            InterestPolicy interestPolicy = new MonthlyInterestPolicy(new BigDecimal(10.5));
            Payment payment = new DepositPayment(new BigDecimal(100), LocalDate.now().plusMonths(3));
            deposit.addPayment(payment, interestPolicy);
        });

        Then("^the interest rate for these funds is 0.5% greater than the original interest rate$", () -> {
            assertThat(deposit.getInterestRates()).containsExactly(new BigDecimal(10), new BigDecimal(10.5));
        });

        And("^the interest for this funds is proportional to the deposit time left$", () -> {
            deposit.closeDeposit(LocalDate.now().plusMonths(6));
            assertThat(account.getBalance()).isCloseTo(new BigDecimal(207.62), Offset.offset(new BigDecimal(0.1)));
        });


    }
}
