Feature: Check BOOLEANS before deleting Accounts by accountId

  Background:
    Given the system is ready and i log with email "admin@admin.com" and password "1234"

# This creates A LOT of accounts
# TODO: create and delete correct account but have un-deletable accounts already present in db ==> change Script
  Scenario: Delete recently created account
    Given the customer creates an account with 200 euros
    When the customer tries to delete the account
    Then the customer should receive the code 200 and a message

  Scenario: Delete blocked account
    Given the customer creates an account with 200 euros
    And the customer blocks the account
    When the customer tries to delete the account
    Then the customer should receive the code 400 and a message

  Scenario: Delete account in debt
    Given the customer creates an account with -200 euros
    When the customer tries to delete the account
    Then the customer should receive the code 400 and a message

  Scenario: Delete account in debt and blocked
    Given the customer creates an account with -200 euros
    And the customer blocks the account
    When the customer tries to delete the account
    Then the customer should receive the code 400 and a message