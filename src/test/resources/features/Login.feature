@login
Feature: NSW Stamp Duty Calculator

  Scenario: Calculate stamp duty for vehicle
    Given user opens the NSW Stamp Duty page
    Then the page should load successfully
    When user clicks on Check online button
    Then Revenue NSW calculators page should appear
    When user selects Yes option
    And user enters purchase price
    And user clicks on Calculate button
    Then calculation result should be displayed correctly

