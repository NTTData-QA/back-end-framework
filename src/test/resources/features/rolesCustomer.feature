Feature: Test endpoints filtered by role

  Scenario Outline: Obtain Customer list
    Given the system is ready and i log with email "<email>" and password "<pwd>"
    When i request all Customers list
    Then i should receive the code <code>
    And if the response is successful, i should receive the customers list

    Examples:
      | email | pwd | code |
      | john.doe@example.com | password123 | 403 |
      | admin@admin.com      | 1234        | 200 |

  @createFakeAccountFirst
  Scenario Outline: Delete Customer's account
    Given the system is ready and i log with email "<email>" and password "<pwd>"
    When i try to delete an another customer's account
    Then i should receive the code <code> and a status message

    Examples:
      | email | pwd | code |
      | john.doe@example.com | password123 | 403 |
      | admin@admin.com      | 1234        | 200 |

