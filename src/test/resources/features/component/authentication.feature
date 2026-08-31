@component @authentication
Feature: User login

  Scenario: Trainer logs in with valid credentials
    Given a registered trainer account exists for component tests
    And the trainer uses the correct password for login
    When the client submits the login request
    Then the response status should be 200
    And the login response should contain a JWT token

  Scenario: Trainer login is rejected with an incorrect password
    Given a registered trainer account exists for component tests
    And the trainer uses an incorrect password for login
    When the client submits the login request
    Then the response status should be 401
    And the error response should mention invalid credentials
