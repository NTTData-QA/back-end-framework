@NTTData
Feature: NTTData webpage


  Scenario: Validate http status code for landing page
    When the user navigates to "https://www.openbank.es/"
    Then the status code is 200