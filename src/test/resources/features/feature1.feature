Feature: User authentication
  As a user, I want to be able to register, login, and logout in the application.

  Background:
    Given the system is ready for user authentication

  Scenario: Register a new customer
    When I register with name "John", surname "Doe", email "probando5@example.com" and password "password123"
    Then I should receive a message "You have registered successfully."

  Scenario: Login with valid credentials
    Given I have registered with name "John", surname "Doe", email "probando6@example.com" and password "password123"
    When I login with email "probando6@example.com" and password "password123"
    Then I should receive a message "Correct authentication"

  Scenario: Login with invalid credentials
    When I login with email "invalid@example.com" and password "wrongpassword"
    Then I should receive a message "Invalid credentials"

  Scenario: Logout after logging in
    Given I have logged in with email "probando3@example.com" and password "password123"
    When I log out
    Then I should receive a message "Logged out successfully. Cookies cleared."
