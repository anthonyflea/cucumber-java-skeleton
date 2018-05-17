package pl.edu.agh.iet.katabank.bankproduct.deposittype;

import java.math.BigDecimal;

public class MonthlyDepositType implements DepositType {

    private int duration;
    private BigDecimal yearlyInterestRate;

    public MonthlyDepositType(int durationInMonths, BigDecimal yearlyInterestRate) {
        this.duration = durationInMonths;
        this.yearlyInterestRate = yearlyInterestRate;
    }

    @Override
    public int getDuration() {
        return this.duration;
    }

    @Override
    public BigDecimal getYearlyInterestRate() {
        return yearlyInterestRate;
    }
}
