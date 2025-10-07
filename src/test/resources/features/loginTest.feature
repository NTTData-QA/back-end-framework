Feature: LoginTest with GenericSteps and AuthenticationTestSteps
  @NoCleanup
  Scenario Outline: The customer logins with a user to test GenericSteps
    When The customer logins with  email "<email>" and  password "<password>"
    Then The customer gets a <status> status response and message: "<message>"

    Examples:
    |email                      |password             |status    |message                         |
    |john.doe@example.com       |password123          |200       |Correct authentication          |
    |john.doe@example.com       |password13           |400       |Invalid credentials             |
