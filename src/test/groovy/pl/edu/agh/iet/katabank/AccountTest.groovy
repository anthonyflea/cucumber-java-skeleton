package pl.edu.agh.iet.katabank

import spock.lang.Specification

import static org.assertj.core.api.Assertions.assertThat

class AccountTest extends Specification{

    private final Customer customer = new Customer()
    private final Account account = new Account(customer)

    def "two accounts created for the same customer are not equal"() {
        expect:
        assertThat(account).isNotEqualTo(anotherAccount)

        where:
        anotherAccount = new Account(customer)
    }

    def "try to withdraw a negative amount"() {
        when:
        account.withdraw(new BigDecimal(-1))

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'The amount to withdraw is negative.'
    }

    def "try to withdraw amount greater than account balance"() {
        setup:
        account.setBalance(new BigDecimal(100))

        when:
        account.withdraw(new BigDecimal(101))

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'The amount to withdraw is greater than account balance.'
    }
}
