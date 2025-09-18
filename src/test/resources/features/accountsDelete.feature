Feature: Check BOOLEANS before deleting Accounts by accountId

  Background:
    Given the system is ready and i log with email "john.doe@example.com" and password "password123"

  Scenario: Delete recently created account
    Given the customer creates an account with 200 euros with status blocked "false"
    When the customer tries to delete the account
    Then i should receive the code 200 and a message

  Scenario: Delete blocked account
    Given the customer creates an account with 200 euros with status blocked "true"
    And the customer tries to delete the account
    Then i should receive the code 400 and a message

  Scenario: Delete account in debt
    Given the customer creates an account with -200 euros with status blocked "false"
    When the customer tries to delete the account
    Then i should receive the code 400 and a message

  Scenario: Delete account in debt and blocked
    Given the customer creates an account with -200 euros with status blocked "true"
    And the customer tries to delete the account
    Then i should receive the code 400 and a message