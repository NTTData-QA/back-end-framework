Feature: Check BOOLEANS before deleting Accounts by accountId

  Background:
    Given the system is ready and i log with email "admin@admin.com" and password "1234"

  Scenario: Delete recently created account
    Given the customer creates an account with 200 euros
    When the customer tries to delete the account
    Then the customer should receive the code 200 and a message

  Scenario: Delete blocked account
    Given a customer has an account with id 170
    When the customer tries to delete the account
    Then The customer gets a 400 status response and message: "Error: Account with id 170 is blocked"

  Scenario: Delete account in debt
    Given a customer has an account with id 171
    When the customer tries to delete the account
    Then The customer gets a 400 status response and message: "Error: Account with id 171 is in debt"

  Scenario: Delete account in debt and blocked
    Given a customer has an account with id 172
    When the customer tries to delete the account
    Then The customer gets a 400 status response and message: "Error: Account with id 172 is in debt and blocked"