Feature: Get Transfers History given an account

  Background:
    Given the system is ready and i log with email "john.doe@example.com" and password "password123"

  Scenario: Logged customer accounts
    When i request this users account information
    Then i should receive the code 200


  Scenario Outline: All logged customer main account Transfers
    When i request all of this users transfers with accountId <accId>
    Then i should receive the code <code>
    And i should receive all of the transfers or an error message
    Examples:
      | accId | code |
      | 1     | 200  |
      | 2     | 403  |
      | 99999 | 400  |
