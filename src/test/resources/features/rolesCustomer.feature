Feature: Test endpoints filtered by role

  Scenario Outline: Obtain Customer list
    Given the system is ready and i log with email "<email>" and password "<pwd>"
    When i request all Customers list
    Then i should receive the code <code>
    And if the response is successful, i should receive the customers list

    Examples:
      | email | pwd | code |
      | jane.smith@example.com | securepass | 403 |
      | john.doe@example.com | password123 | 200 |

  @createFakeAccountFirst
  Scenario Outline: Delete Customer's account
    Given the system is ready and i log with email "<email>" and password "<pwd>"
    When i try to delete an account with id 33
    Then i should receive the code <code> and a status message

    Examples:
      | email | pwd | code |
      | jane.smith@example.com | securepass | 403 |
      | john.doe@example.com | password123 | 200 |

