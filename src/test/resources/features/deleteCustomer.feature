Feature: Delete a customer

  Background:
    Given the system is ready for user authentication

  @NoCleanup
  Scenario: Register a new customer with a new account and delete his customer registration by id
    Given I have registered with name "paula", surname "Calvente", email "paula@example.com" and password "password"
    And I login with email "paula@example.com" and password "password"
    When The customer deletes his customer registration by id
    Then The customer gets a 200 status response



