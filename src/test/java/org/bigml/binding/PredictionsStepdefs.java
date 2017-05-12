package org.bigml.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

import org.bigml.binding.resources.AbstractResource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

public class PredictionsStepdefs {

    // Logging
    Logger logger = LoggerFactory.getLogger(PredictionsStepdefs.class);

    CommonStepdefs commonSteps = new CommonStepdefs();

    @Autowired
    private ContextRepository context;

    @When("^I create a proportional missing strategy prediction by name=(true|false) for \"(.*)\"$")
    public void I_create_a_proportional_missing_strategy_prediction(String by_name, String inputData)
            throws AuthenticationException {
        String modelId = (String) context.model.get("resource");
        Boolean byName = new Boolean(by_name);

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));
        args.put("missing_strategy", 1);

        JSONObject resource = BigMLClient.getInstance().createPrediction(
                modelId, (JSONObject) JSONValue.parse(inputData), byName,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.prediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @When("^I create a prediction by name=(true|false) for \"(.*)\"$")
    public void I_create_a_prediction(String by_name, String inputData)
            throws AuthenticationException {
        String modelId = (String) context.model.get("resource");
        Boolean byName = new Boolean(by_name);

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createPrediction(
                modelId, (JSONObject) JSONValue.parse(inputData), byName,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.prediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Given("^I get the prediction \"(.*)\"")
    public void I_get_the_prediction(String predictionId)
            throws AuthenticationException {
        JSONObject resource = BigMLClient.getInstance().getPrediction(
                predictionId);
        Integer code = (Integer) resource.get("code");
        assertEquals(AbstractResource.HTTP_OK, code.intValue());
        context.prediction = (JSONObject) resource.get("object");
    }

    @Then("^the numerical prediction for \"([^\"]*)\" is ([\\d,.]+)$")
    public void the_numerical_prediction_for_is(String objectiveField, double pred) {
        JSONObject obj = (JSONObject) context.prediction.get("prediction");
        String predictionValue = String.format("%.5g", ((Double) obj.get(objectiveField)));

        assertEquals(String.format("%.5g", pred), predictionValue);
    }

    @Then("^the prediction for \"([^\"]*)\" is \"([^\"]*)\"$")
    public void the_prediction_for_is(String objectiveField, String pred) {
        JSONObject obj = (JSONObject) context.prediction.get("prediction");
        String objective = (String) obj.get(objectiveField);
        assertEquals(pred, objective);
    }

    @Then("^the confidence for the prediction is ([\\d,.]+)$")
    public void the_confidence_for_the_prediction_is(Double expectedConfidence) {
        Double actualConfidence = (Double) context.prediction.get("confidence");
        assertEquals(String.format("%.4g", expectedConfidence), String.format("%.4g",actualConfidence));
    }

    @When("^I create a prediction with ensemble for \"(.*)\"$")
    public void I_create_a_prediction_with_ensemble_for(String inputData) throws AuthenticationException {
        String ensembleId = (String) context.ensemble.get("resource");

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createPrediction(
                ensembleId, (JSONObject) JSONValue.parse(inputData), true,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.prediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @When("^I create a prediction with ensemble by name=(true|false) for \"(.*)\"$")
    public void I_create_a_prediction_with_ensemble_for(String by_name,
            String inputData) throws AuthenticationException {
        String ensembleId = (String) context.ensemble.get("resource");
        Boolean byName = new Boolean(by_name);

        JSONObject args = new JSONObject();
        args.put("tags", Arrays.asList("unitTest"));

        JSONObject resource = BigMLClient.getInstance().createPrediction(
                ensembleId, (JSONObject) JSONValue.parse(inputData), byName,
                args, 5, null);
        context.status = (Integer) resource.get("code");
        context.location = (String) resource.get("location");
        context.prediction = (JSONObject) resource.get("object");
        commonSteps.the_resource_has_been_created_with_status(context.status);
    }

    @Then("^the prediction with ensemble for \"([^\"]*)\" is \"([^\"]*)\"$")
    public void the_prediction_with_ensemble_for_is(String expected, String pred) {
        JSONObject obj = (JSONObject) context.prediction.get("prediction");
        String objective = (String) obj.get(expected);
        assertEquals(pred, objective);
    }

    @Given("^I wait until the predition status code is either (\\d) or (\\d) less than (\\d+)")
    public void I_wait_until_prediction_status_code_is(int code1, int code2,
            int secs) throws AuthenticationException {
        Long code = (Long) ((JSONObject) context.prediction.get("status"))
                .get("code");
        GregorianCalendar start = new GregorianCalendar();
        start.add(Calendar.SECOND, secs);
        Date end = start.getTime();
        while (code.intValue() != code1 && code.intValue() != code2) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            assertTrue("Time exceded ", end.after(new Date()));
            I_get_the_prediction((String) context.prediction.get("resource"));
            code = (Long) ((JSONObject) context.prediction.get("status"))
                    .get("code");
        }
        assertEquals(code1, code.intValue());
    }

    @Given("^I wait until the prediction is ready less than (\\d+) secs$")
    public void I_wait_until_the_prediction_is_ready_less_than_secs(int secs)
            throws AuthenticationException {
        I_wait_until_prediction_status_code_is(AbstractResource.FINISHED,
                AbstractResource.FAULTY, secs);
    }

    @Given("^I combine the votes in \"(.*)\"$")
    public void I_combine_the_votes(String dir)
            throws AuthenticationException {
        try {
            context.votes = context.multiModel.batchVotes(dir, null);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Exception combining votes", false);
        }
    }

    @Given("^the plurality combined predictions are \"(.*)\"$")
    public void The_plurality_combined_prediction(String predictionsStr)
            throws AuthenticationException {
        JSONArray predictions = (JSONArray) JSONValue.parse(predictionsStr);
        for (int iVote = 0; iVote < context.votes.size(); iVote++ ) {
            MultiVote vote = context.votes.get(iVote);
            Map<Object,Object> combinedPrediction = vote.combine();
            assertEquals("The predictions are not equals", predictions.get(iVote),
                    combinedPrediction.get("prediction"));
        }
    }

    @Given("^the confidence weighted predictions are \"(.*)\"$")
    public void The_confidence_weighted_prediction(String predictionsStr)
            throws AuthenticationException {
        JSONArray predictions = (JSONArray) JSONValue.parse(predictionsStr);
        for (int iVote = 0; iVote < context.votes.size(); iVote++ ) {
            MultiVote vote = context.votes.get(iVote);
            Map<Object,Object> combinedPrediction = vote.combine(PredictionMethod.CONFIDENCE, false,
                    null, null, null, null, null);
            assertEquals("The predictions are not equals", predictions.get(iVote),
                    combinedPrediction.get("prediction"));
        }
    }


    @Then("^I create a local mm median batch prediction using \"(.*)\" with prediction (.*)$")
    public void i_create_a_local_mm_median_batch_prediction_using_with_prediction(String args, Double expectedPrediction)
            throws Exception {
        JSONObject inputData = (JSONObject) JSONValue.parse(args);
        JSONArray inputDataList = new JSONArray();
        inputDataList.add(inputData);
        List<MultiVote> votes = context.multiModel.batchPredict(inputDataList, null, true, false, MissingStrategy.LAST_PREDICTION,
                null, false, true);

        Double prediction = (Double) votes.get(0).getPredictions()[0].get("prediction");
        assertEquals(expectedPrediction, prediction);
    }

//    @Given("^I create a batch prediction for \"(.*)\" and save it in \"(.*)\"$")
//    public void i_create_a_batch_prediction_for_and_save_it_in(String inputDataList, String directory)
//            throws AuthenticationException {
//        if( directory != null && directory.length() > 0 ) {
//            File dirFile = new File(directory);
//            if( !dirFile.exists() ) {
//                dirFile.mkdirs();
//            }
//        }
//    }

}