@api
Feature: Validate OpenLibrary Author API

  Scenario: Validate personal and alternate names
    Given user calls OpenLibrary author API
    Then personal_name should be validated
    And alternate_names should contain expected
