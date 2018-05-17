package pl.edu.agh.iet.katabank.bankproduct.deposittype;

import java.math.BigDecimal;

public interface DepositType {

    int getDuration();
    BigDecimal getYearlyInterestRate();

}
