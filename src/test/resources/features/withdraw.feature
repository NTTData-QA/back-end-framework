Feature: Withdraws end-to-end (crear, listar, límites diario/mensual y actualización de límites)

  Background:
    Given The customer registers with 1 accounts, 1 cards and an initial amount of 2000.0
    And The customer logs in with their register credentials
    And I set the test cardId to my first registered card

  @withdraw_ok
  Scenario: Crear un withdraw dentro del límite diario y mensual
    # Ponemos límites amplios para que no molesten en este caso
    And I update the daily limit to 1000.0 for the current card
    And I update the monthly limit to 5000.0 for the current card
    And I create a withdraw of 50.0 EUR with the current card
    Then The customer gets a 200 status response

  @daily_limit_exceeded
  Scenario: Superar el límite diario
    # Fijamos el límite diario a un valor conocido y pequeño
    And I update the daily limit to 300.0 for the current card
    # Hacemos 250 + 100 = 350 -> debe exceder 300
    And I create a withdraw of 250.0 EUR with the current card
    Then The customer gets a 200 status response
    And I create a withdraw of 100.0 EUR with the current card
    Then The customer gets a 400 status response and message: "Saldo diario insuficiente, saldo restante diario: 50.0"

  @monthly_limit_exceeded
  Scenario: Superar el límite mensual
    # Evita que el límite diario interfiera en la prueba mensual
    And I update the daily limit to 10000.0 for the current card
    # Fija el límite mensual a 1500 y luego intenta 1600
    And I update the monthly limit to 1500.0 for the current card
    And I repeatedly create withdraws summing to 1600.0 EUR with the current card
    Then The customer gets a 400 status response and message: "Saldo mensual insuficiente, saldo restante mensual: 100.0"

  @update_limits_and_retry
  Scenario: Actualizar límites y volver a intentar
    And I update the daily limit to 500.0 for the current card
    And I update the monthly limit to 2000.0 for the current card
    And I create a withdraw of 400.0 EUR with the current card
    Then The customer gets a 200 status response