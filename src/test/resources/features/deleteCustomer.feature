Feature: Delete a customer

  Background:
    Given the system is ready for user authentication

  @NoCleanup #Tag para que el escenario no ejecute el After que elimina el usuario.
  Scenario: Register a new customer with a new account and delete his customer registration by id
    Given I have registered with name "paula", surname "Calvente", email "paula@example.com" and password "password"
    And I login with email "admin@admin.com" and password "1234"
    When The customer deletes his customer registration by id
    Then The customer gets a 200 status response



