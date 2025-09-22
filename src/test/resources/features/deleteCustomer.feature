Feature: Delete a customer

  Scenario: Register a new customer with a new account and delete his customer registration by id
    Given I register with name "Paula", surname "Calvente", email "paula@example.com" and password "password123" and I log in
    When The customer deletes his customer registration by id
    Then I should receive a message "The customer has been deleted successfully."





