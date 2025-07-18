@NTTData
Feature: NTTData webpage


  Scenario: Validate http status code for NTTData landing page
    When the user navigates to "https://es.nttdata.com/"
    Then the status code is 200