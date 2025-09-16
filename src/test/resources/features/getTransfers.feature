Feature: Get Transfers History given an account

  Background:
    Given the system is ready and i log with email "john.doe@example.com" and password "password123"

  Scenario: Logged customer accounts
    When i request this users account information
    Then i should receive the code 200

#  Scenario: Logged customer main account sent Transfers
#    When i request this users main account sent transfers
#    Then i should receive the sent transfers

#  Scenario: Logged customer main account received Transfers
#    When i request this users main account received transfers
#    Then i should receive the received transfers

  Scenario: All logged customer main account Transfers
    When i request all of this users main account transfers
    Then i should receive all of the transfers