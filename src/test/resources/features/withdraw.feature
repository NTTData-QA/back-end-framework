Feature: Withdraws tests

  Background:
    Given The customer registers with 1 accounts, 1 cards and an initial amount of 2000.0
    And The customer logs in with their register credentials
    And I set the test cardId to my first registered card

  Scenario: Create a withdrawal within the daily and monthly limit
    When I update the daily limit to 1000.0 for the current card
    And I update the monthly limit to 5000.0 for the current card
    And I create a withdraw of 50.0 EUR with the current card
    Then The customer gets a 200 status response

  Scenario: Surpass the daily limit
    When I update the daily limit to 300.0 for the current card
    And I create a withdraw of 250.0 EUR with the current card
    Then The customer gets a 200 status response
    When I create a withdraw of 100.0 EUR with the current card
    Then The customer gets a 400 status response and message: "Saldo diario insuficiente, saldo restante diario: 50.0"

  Scenario: Surpass the monthly limit
    When I update the daily limit to 10000.0 for the current card
    And I update the monthly limit to 1500.0 for the current card
    And I repeatedly create withdraws summing to 1600.0 EUR with the current card
    Then The customer gets a 400 status response and message: "Saldo mensual insuficiente, saldo restante mensual: 100.0"

  Scenario: Update limits and try again
    When I update the daily limit to 500.0 for the current card
    And I update the monthly limit to 2000.0 for the current card
    And I create a withdraw of 400.0 EUR with the current card
    Then The customer gets a 200 status response