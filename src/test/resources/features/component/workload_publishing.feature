@component @messaging
Feature: Trainer workload notification publishing

  Scenario: Valid workload event is sent to the main queue
    When a valid workload add event is published
    Then the main workload queue should contain an ADD event
    And the workload event should include trainer username "component.trainer"

  Scenario: Invalid workload event is routed to the dead-letter queue
    When an invalid workload add event is published
    Then the invalid workload queue should contain the workload event
    And the main workload queue should stay empty
    And the workload request should be considered invalid by the validator
