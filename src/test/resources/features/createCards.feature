Feature: Testing the cards creation registering a new user with random parameters.

  Scenario Outline: A customer registers, creates N Accounts and M cards associated to those accounts.
    Given The customer registers with <naccounts> accounts, <ncards> cards and an initial amount of <amount>
    When The customer logs in with their register credentials
    And The customer checks their cards
    Then The customer gets a <status> status response

    Examples:
    |naccounts    |ncards |amount   |status   |
    |1            |1      |140      |200      |
    |1            |0      |20       |200      |
    |1            |4      |130      |200      |
    |2            |2      |231      |200      |

  Scenario: Creating a card to your own account
    Given I have logged in with email "john.doe@example.com" and password "password123"
    When I create a card to the account: 1
    Then The customer gets a 200 status response and message: "Card created successfully"


  Scenario: Creating a card to other one account
    Given I have logged in with email "john.doe@example.com" and password "password123"
    When I create a card to the account: 2
    Then The customer gets a 400 status response and message: "Error creating card: you can only create card to your account"