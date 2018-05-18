package pl.edu.agh.iet.katabank.bankproduct

import pl.edu.agh.iet.katabank.Customer
import pl.edu.agh.iet.katabank.bankproduct.deposittype.DepositType
import pl.edu.agh.iet.katabank.bankproduct.deposittype.MonthlyDepositType
import spock.lang.Specification

import java.time.LocalDate

import static org.assertj.core.api.Assertions.assertThat

class DepositTest extends Specification {

    private static final String ERROR_MESSAGE_OPEN_DEPOSIT = 'Incorrect initial balance to open deposit: '
    private static final String ERROR_MESSAGE_CLOSE_DEPOSIT_ON_DATE = 'Cannot close deposit on date: '
    private static final String ERROR_MESSAGE_CLOSE_DEPOSIT_ALREADY_CLOSED = 'Cannot close already closed deposit'

    private final Customer customer = new Customer()
    private final Account account = new Account(customer)
    private int durationInMonths = 12
    private DepositType depositType = new MonthlyDepositType(durationInMonths, 10.0)
    private Deposit deposit
    private BigDecimal amount
    private LocalDate openDate

    def "two deposits created for the same account are not equal"() {
        when:
        amount = 50
        account.setBalance(200.0)
        def firstDeposit = new Deposit(account, amount, depositType)
        def secondDeposit = new Deposit(account, amount, depositType)

        then:
        assertThat(firstDeposit).isNotEqualTo(secondDeposit)
    }

    def "deposit created has the same owner as connected account"() {
        when:
        account.setBalance(amount)
        deposit = new Deposit(account, amount, depositType)

        then:
        assertThat(deposit.getOwner()).isEqualTo(account.getOwner())

        where:
        amount = 10.0
    }

    def "try to open a deposit with a negative balance"() {
        when:
        amount = -1
        account.setBalance(100.0)
        deposit = new Deposit(account, amount, depositType)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == ERROR_MESSAGE_OPEN_DEPOSIT + amount
    }

    def "try to open a deposit with a zero balance"() {
        when:
        amount = 0
        account.setBalance(100.0)
        deposit = new Deposit(account, amount, depositType)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == ERROR_MESSAGE_OPEN_DEPOSIT + amount
    }

    def "try to open a deposit for amount greater than the account balance"() {
        when:
        amount = 100
        account.setBalance(amount - 1)
        deposit = new Deposit(account, amount, depositType)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == ERROR_MESSAGE_OPEN_DEPOSIT + amount
    }

    def "try to open a deposit for a null amount"() {
        when:
        account.setBalance(100.0)
        deposit = new Deposit(account, amount, depositType)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == ERROR_MESSAGE_OPEN_DEPOSIT + amount
    }

    def "default deposit is open with today's date"() {
        when:
        account.setBalance(amount)
        deposit = new Deposit(account, amount, depositType)

        then:
        deposit.getOpenDate().isEqual(LocalDate.now())

        where:
        amount = 10.0
    }

    def "deposit cannot be closed before duration passed"() {
        when:
        account.setBalance(amount)
        def closeDate = openDate.plusMonths(durationInMonths - 1)
        deposit = new Deposit(account, amount, openDate, depositType)
        deposit.closeDeposit(closeDate)

        then:
        RuntimeException ex = thrown()
        ex.message == ERROR_MESSAGE_CLOSE_DEPOSIT_ON_DATE + closeDate

        where:
        amount = 10.0
        openDate = LocalDate.now()
    }

    def "deposit cannot be closed when passed close date is null"() {
        when:
        account.setBalance(amount)
        def closeDate = null
        deposit = new Deposit(account, amount, openDate, depositType)
        deposit.closeDeposit(closeDate)

        then:
        RuntimeException ex = thrown()
        ex.message == ERROR_MESSAGE_CLOSE_DEPOSIT_ON_DATE + closeDate

        where:
        amount = 10.0
        openDate = LocalDate.now()
    }

    def "when deposit closed balance is zero"() {
        when:
        account.setBalance(amount)
        def closeDate = openDate.plusMonths(durationInMonths)
        deposit = new Deposit(account, amount, openDate, depositType)
        deposit.closeDeposit(closeDate)

        then:
        assertThat(deposit.getBalance()).isZero()

        where:
        amount = 10.0
        openDate = LocalDate.now()
    }

    def "deposit can be closed when exactly duration time passed"() {
        when:
        account.setBalance(amount)
        def closeDate = openDate.plusMonths(durationInMonths)
        deposit = new Deposit(account, amount, openDate, depositType)
        deposit.closeDeposit(closeDate)

        then:
        assertThat(deposit.isOpen()).isFalse()

        where:
        amount = 10.0
        openDate = LocalDate.now()
    }

    def "deposit can be closed when exactly duration time plus one day passed"() {
        when:
        account.setBalance(amount)
        def closeDate = openDate.plusMonths(durationInMonths).plusDays(1)
        deposit = new Deposit(account, amount, openDate, depositType)
        deposit.closeDeposit(closeDate)

        then:
        assertThat(deposit.isOpen()).isFalse()

        where:
        amount = 10.0
        openDate = LocalDate.now()
    }

    def "when deposit created it is open"() {
        when:
        account.setBalance(amount)
        deposit = new Deposit(account, amount, depositType)

        then:
        assertThat(deposit.isOpen()).isTrue()

        where:
        amount = 10.0
    }

    def "deposit already closed can't be closed"() {
        when:
        account.setBalance(amount)
        def closeDate = openDate.plusMonths(durationInMonths)
        deposit = new Deposit(account, amount, openDate, depositType)
        deposit.closeDeposit(closeDate)
        deposit.closeDeposit(closeDate.plusDays(1))

        then:
        assertThat(deposit.isOpen()).isFalse()
        RuntimeException ex = thrown()
        ex.message == ERROR_MESSAGE_CLOSE_DEPOSIT_ALREADY_CLOSED

        where:
        amount = 10.0
        openDate = LocalDate.now()
    }

    def "when deposit created it returns correct close date"() {
        when:
        account.setBalance(amount)
        deposit = new Deposit(account, amount, openDate, depositType)
        def closeDate = openDate.plusMonths(6)

        then:
        assertThat(deposit.getCloseDate()).isEqualTo(closeDate)

        where:
        amount = 10.0
        openDate = LocalDate.now().plusDays(1)
        depositType = new MonthlyDepositType(6, 5.0)
    }

    def "when deposit created it returns correct deposit type"() {
        when:
        account.setBalance(amount)
        deposit = new Deposit(account, amount, openDate, depositType)

        then:
        assertThat(deposit.getDepositType()).isEqualTo(depositType)

        where:
        amount = 10.0
        depositType = new MonthlyDepositType(6, 5.0)
    }

    def "when deposit created it returns correct interest rate"() {
        when:
        account.setBalance(amount)
        deposit = new Deposit(account, amount, openDate, depositType)

        then:
        assertThat(deposit.yearlyInterestRatePercent).isEqualTo(5.0)

        where:
        amount = 10.0
        depositType = new MonthlyDepositType(6, 5.0)
    }

}
