@integration @training-integration
Feature: Training workflow publishes workload updates

  Scenario: Authenticated trainer creates training and workload event is published
    Given a trainer and trainee are registered for integration tests
    And the trainer is authenticated for integration tests
    And a valid add training request is prepared
    When the trainer creates the training through the API
    Then the response status should be 200
    And the training should be persisted
    And an ADD workload event should be published after commit
    And the published workload event should refer to the trainer and training

  Scenario: Invalid training request is rejected before persistence and messaging
    Given a trainer and trainee are registered for integration tests
    And the trainer is authenticated for integration tests
    And an invalid add training request with blank training name is prepared
    When the trainer creates the training through the API
    Then the response status should be 400
    And the response should mention validation failure
    And no training should be persisted
    And no workload event should be published

  Scenario: Trainee cannot create training because of role restrictions
    Given a trainer and trainee are registered for integration tests
    And the trainee is authenticated for integration tests
    And a valid add training request is prepared
    When the trainee tries to create the training through the API
    Then the response status should be 403
    And the response should mention forbidden access
    And no training should be persisted
    And no workload event should be published
    And the registered trainer should remain active
