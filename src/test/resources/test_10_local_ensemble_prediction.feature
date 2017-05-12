Feature: Create Predictions locally from Ensembles
  In order to create a local prediction from an ensemble
  I need to create an Ensemble first

  Scenario Outline: Successfully creating a local prediction from an Ensemble:
    Given that I use production mode with seed="<seed>"
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create an ensemble of <number_of_models> models and <tlp> tlp
    And I wait until the ensemble is ready less than <time_3> secs
    And I create a local ensemble
    #When I create a local ensemble prediction with confidence for "<data_input>"
    When the local ensemble prediction for "<data_input>" is "<prediction>"
    #And the local prediction's confidence is "<confidence>"
    Then delete test data

    Examples:
      | data             | seed      |  time_1  | time_2 | time_3 | number_of_models | tlp   |  data_input    |prediction  | confidence |
      | data/iris.csv | BigML |  10      | 10     | 50     | 5                | 1     | {"petal width": 0.5} | Iris-versicolor | 0.3687 |


  Scenario Outline: Successfully obtaining field importance from an Ensemble:
    Given that I use production mode with seed="<seed>"
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create a model with "<parms1>"
    And I wait until the model is ready less than <time_3> secs
    And I create a model with "<parms2>"
    And I wait until the model is ready less than <time_4> secs
    And I create a model with "<parms3>"
    And I wait until the model is ready less than <time_5> secs
    When I create a local Ensemble with the last <number_of_models> models
    Then the field importance text is "<field_importance>"
    Then delete test data

    Examples:
      | data             | seed      |  time_1  | time_2 |parms1 | time_3 |parms2 | time_4 |parms3| time_5 |number_of_models |field_importance |
      | data/iris.csv | BigML        |  10      | 10     |{"input_fields": ["000000", "000001","000003", "000004"]} |20      |{"input_fields": ["000000", "000001","000002", "000004"]} | 20     |{"input_fields": ["000000", "000001","000002", "000003", "000004"]} | 20   | 3 |[["000002", 0.5269933333333333], ["000003", 0.38936], ["000000", 0.04662333333333333], ["000001", 0.037026666666666666]] |


  Scenario Outline: Successfully creating a local prediction from an Ensemble adding confidence:
    Given that I use production mode with seed="<seed>"
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create an ensemble of <number_of_models> models and <tlp> tlp
    And I wait until the ensemble is ready less than <time_3> secs
    And I create a local ensemble
    #When I create a local ensemble prediction for "<data_input>" in JSON adding confidence
    When the local ensemble prediction for "<data_input>" is "<prediction>" with confidence <confidence>
    #And the local prediction's confidence is "<confidence>"
    Then delete test data

    Examples:
      | data            | seed    |  time_1  | time_2 | time_3 | number_of_models | tlp   |  data_input           |prediction       | confidence  |
      | data/iris.csv   | BigML   |  10      | 10     | 50     | 5                | 1     | {"petal width": 0.5}  | Iris-versicolor |  0.3687     |


  # THIS SCENARIO IS NOT NECESSARY IN THE JAVA BINDING BECAUSE WE ONLY CAN CREATE LOCALENSEMBLES
  #  USING A LIST OF MODEL IDs. THIS IS ENOUGH TO CREATE THE LOCAL ENSEMBLES.
  #
  #  Scenario Outline: Successfully obtaining field importance from an Ensemble created from local models:
  #    Given that I use production mode with seed="<seed>"
  #    Given I create a data source uploading a "<data>" file
  #    And I wait until the source is ready less than <time_1> secs
  #    And I add the unitTest tag to the data source waiting less than <time_1> secs
  #    And I create a dataset
  #    And I wait until the dataset is ready less than <time_2> secs
  #    And I create a model with "<parms1>"
  #    And I wait until the model is ready less than <time_3> secs
  #    And I create a model with "<parms2>"
  #    And I wait until the model is ready less than <time_4> secs
  #    And I create a model with "<parms3>"
  #    And I wait until the model is ready less than <time_5> secs
  #    When I create a local Ensemble with the last <number_of_models> local models
  #    Then the field importance text is "<field_importance>"
  #    Then delete test data
  #
  #    Examples:
  #      | data             | seed      |  time_1  | time_2 |parms1 | time_3 |parms2 | time_4 |parms3| time_5 |number_of_models |field_importance |
  #      | data/iris.csv | BigML        |  10      | 10     |{"input_fields": ["000000", "000001","000003", "000004"]} |20      |{"input_fields": ["000000", "000001","000002", "000004"]} | 20     |{"input_fields": ["000000", "000001","000002", "000003", "000004"]} | 20   | 3 |[["000002", 0.5269933333333333], ["000003", 0.38936], ["000000", 0.04662333333333333], ["000001", 0.037026666666666666]] |


  Scenario Outline: Successfully creating a local prediction from an Ensemble:
    Given that I use production mode with seed="<seed>"
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create an ensemble of <number_of_models> models and <tlp> tlp
    And I wait until the ensemble is ready less than <time_3> secs
    And I create a local ensemble
    When the local ensemble prediction using median with confidence for "<data_input>" is "<prediction>"
    Then delete test data

    Examples:
      | data            | seed      |  time_1  | time_2 | time_3 | number_of_models | tlp   |  data_input    |prediction  |
      | data/grades.csv   | BigML     |  10      | 10     | 50     | 2                | 1     | {}             | 67.5    |


  Scenario Outline: Successfully creating a local prediction from an Ensemble with max models:
    Given that I use production mode with seed="<seed>"
    Given I create a data source uploading a "<data>" file
    And I wait until the source is ready less than <time_1> secs
    And I add the unitTest tag to the data source waiting less than <time_1> secs
    And I create a dataset
    And I wait until the dataset is ready less than <time_2> secs
    And I create an ensemble of <number_of_models> models and <tlp> tlp
    And I wait until the ensemble is ready less than <time_3> secs
    And I create a local ensemble with max models <max_models>
    When the local ensemble prediction for "<data_input>" is "<prediction>"

    Examples:
      | data            | seed    |  time_1  | time_2 | time_3 | time_4 | number_of_models | tlp   | max_models |  data_input    |prediction  |
      | data/iris.csv   | BigML   |  10      | 10     | 50     | 20     | 5                | 1     | 2          | {"petal width": 0.5} | Iris-versicolor |
