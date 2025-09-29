Feature: Check Transfer Not Enough Money in Sender User

  Scenario Outline: A customer with an account with less than 1000€ does a transfer to another user of 1500€
    Given The customer registers with <numberOfAccounts> accounts, <ncards> cards and an initial amount of <money>
    And The customer logs in with their register credentials
    #And The receiving customer has an account with id <receiverAccountId>
    When The customer make a transfer with their main account and transferAmount <transferAmount> to an account with id <receiverAccountId>
    Then The customer gets a <status> status response and message: <message>

    Examples:
    |numberOfAccounts|ncards|money   |receiverAccountId    |transferAmount     |status   |message                                                                      |
    |1               |0     |800     |2                    |1500               |400      |"Transfer cannot be completed. Not enough money or blocked receiver."        |
