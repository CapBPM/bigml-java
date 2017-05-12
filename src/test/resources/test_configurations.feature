Feature: Testing Configuration REST api calls
    In order to test the Configuration API
    I need to create a configuration

    Scenario Outline: Successfully creating a configuration:
        Given that I use production mode with seed="<seed>"
        Given I create a configuration with "<options>"
        And I wait until the configuration is ready less than <time_1> secs
        And I check the configuration name <name>
        And I update the configuration with "<params>"
        And I check the configuration name <new_name>
        And I delete the configuration
        Then delete test data

        Examples:
        | seed  | time_1  | options                | name   |   params                            |   new_name             |
        | BigML |   50    | {"name": "my configuration", "configurations": {"ensemble": {"number_of_models": 10}, "any": {"tags": ["faq"]}}}    | "my configuration"    |   {"name": "my new configuration"}    | "my new configuration"   |
