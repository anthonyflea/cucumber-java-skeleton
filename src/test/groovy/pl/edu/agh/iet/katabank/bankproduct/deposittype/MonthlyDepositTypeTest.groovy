package pl.edu.agh.iet.katabank.bankproduct.deposittype

import pl.edu.agh.iet.katabank.bankproduct.Deposit
import spock.lang.Specification

import java.math.RoundingMode
import java.time.LocalDate

import static org.assertj.core.api.Assertions.assertThat

class MonthlyDepositTypeTest extends Specification {

    private DepositType depositType

    def "close date is calculated correct"() {
        when:
        def duration = 7
        def interestRate = 7.5
        def date = LocalDate.now()
        depositType = new MonthlyDepositType(duration, interestRate)

        then:
        assertThat(depositType.calculateCloseDate(date)).isEqualTo(date.plusMonths(7))

    }

    def "interest is calculated correct"() {
        when:
        def duration = 5
        def interestRate = 10.0
        def initialAmount = 100.0
        depositType = new MonthlyDepositType(duration, interestRate)

        then:
        assertThat(depositType.calculateInterest(initialAmount))
                .isEqualByComparingTo((initialAmount * (interestRate / 100.0) * (duration / 12.0))
                .setScale(2, RoundingMode.HALF_DOWN))

    }

}
