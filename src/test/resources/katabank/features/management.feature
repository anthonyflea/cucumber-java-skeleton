Feature: Bank Management

  Background:
    Given there is a bank with account
    Given there is a customer
    Given there is a default deposit type

  Scenario: list accounts
    Given a customer has two accounts open
    When he lists his accounts
    Then only those accounts are on the list

  Scenario: open account
    Given a customer wants to open an account
    When his account is created
    Then there is a new account on his account list
    And the balance on this account is 0

  Scenario: deposit money
    Given balance on the account is 100
    When customer deposits 10 to this account
    Then balance on the account is 110

  Scenario: withdraw money
    Given balance on the account is 100
    When customer withdraws 90 from this account
    Then balance on the account is 10

  Scenario: transfer money
    Given balance on account A is 100
    And balance on account B is 1000
    When 99.91 is transferred from account A to B
    Then balance after transfer on account A is 0.09
    And balance after transfer on account B is 1099.91

  Scenario: Opening deposit
    Given a customer has an account with balance 100
    When he opens a deposit with balance 90
    Then he owns a deposit with balance 90
    And the account has balance 10

  Scenario: Termination date
    Given a customer opened a deposit for a period of one year
    When one year has passed
    Then the money is transferred back to the account the funds were taken from

  Scenario: Interest rate
    Given bank offers a deposit for a period of 6 months with yearly interest rate 10%
    And customer opens that deposit with funds 100
    When a termination date has passed
    Then 105 is transferred back to his account