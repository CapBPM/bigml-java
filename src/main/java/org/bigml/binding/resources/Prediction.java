package org.bigml.binding.resources;

import java.util.Iterator;

import org.bigml.binding.BigMLClient;
import org.bigml.binding.utils.CacheManager;
import org.bigml.binding.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Entry point to create, retrieve, list, update, and delete predictions.
 *
 * Full API documentation on the API can be found from BigML at:
 * https://bigml.com/api/predictions
 *
 *
 */
public class Prediction extends AbstractResource {

    // Logging
    Logger logger = LoggerFactory.getLogger(Prediction.class);

    /**
     * Constructor
     *
     */
    public Prediction() {
        this.bigmlUser = System.getProperty("BIGML_USERNAME");
        this.bigmlApiKey = System.getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = false;
        super.init(null);
    }

    /**
     * Constructor
     *
     */
    public Prediction(final String apiUser, final String apiKey,
            final boolean devMode) {
        this.bigmlUser = apiUser != null ? apiUser : System
                .getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : System
                .getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = devMode;
        super.init(null);
    }

    /**
     * Constructor
     *
     */
    public Prediction(final String apiUser, final String apiKey,
            final boolean devMode, final CacheManager cacheManager) {
        this.bigmlUser = apiUser != null ? apiUser : System
                .getProperty("BIGML_USERNAME");
        this.bigmlApiKey = apiKey != null ? apiKey : System
                .getProperty("BIGML_API_KEY");
        bigmlAuth = "?username=" + this.bigmlUser + ";api_key="
                + this.bigmlApiKey + ";";
        this.devMode = devMode;
        super.init(cacheManager);
    }

    /**
     * Check if the current resource is a Prediction
     *
     * @param resource the resource to be checked
     * @return true if it's a Prediction
     */
    @Override
    public boolean isInstance(JSONObject resource) {
        return ((String) resource.get("resource")).matches(PREDICTION_RE);
    }

    /**
     * Creates a new prediction.
     *
     * POST
     * /andromeda/prediction?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param model
     *            a unique identifier in the form model/id, ensemble/id or
     *            logisticregression/id where id is a string of 24 alpha-numeric
     *            chars for the nodel, nsemble or logisticregression to attach
     *            the prediction.
     * @param inputData
     *            an object with field's id/value pairs representing the
     *            instance you want to create a prediction for.
     * @param byName
     * @param args
     *            set of parameters for the new prediction. Required
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for model before to start to create the prediction. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    @Deprecated
    public JSONObject create(final String model,
            JSONObject inputData, Boolean byName, String args,
            Integer waitTime, Integer retries) {
        JSONObject argsJSON = (JSONObject) JSONValue.parse(args);
        return create(model, inputData, byName, argsJSON, waitTime, retries);
    }

    /**
     * Creates a new prediction.
     *
     * POST
     * /andromeda/prediction?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param model
     *            a unique identifier in the form model/id, ensemble/id or
     *            logisticregression/id where id is a string of 24 alpha-numeric
     *            chars for the nodel, nsemble or logisticregression to attach
     *            the prediction.
     * @param inputData
     *            an object with field's id/value pairs representing the
     *            instance you want to create a prediction for.
     * @param byName
     * @param args
     *            set of parameters for the new prediction. Required
     * @param waitTime
     *            time (milliseconds) to wait for next check of FINISHED status
     *            for model before to start to create the prediction. Optional
     * @param retries
     *            number of times to try the operation. Optional
     *
     */
    public JSONObject create(final String model,
            JSONObject inputData, Boolean byName, JSONObject args,
            Integer waitTime, Integer retries) {

        JSONObject modelJSON = null;

        if (model == null || model.length() == 0 ||
            !(model.matches(MODEL_RE) || model.matches(ENSEMBLE_RE) || model.matches(LOGISTICREGRESSION_RE))) {
            logger.info("Wrong model, ensemble or logisticregression id");
            return null;
        }

        try {
            waitTime = waitTime != null ? waitTime : 3000;
            retries = retries != null ? retries : 10;

            if (model.matches(ENSEMBLE_RE)) {
                JSONObject ensembleObj = BigMLClient.getInstance(this.devMode).getEnsemble(
                        model);

                if (ensembleObj != null) {
                    modelJSON = ensembleObj;
                    if (waitTime > 0) {
                        int count = 0;
                        while (count < retries
                                && !BigMLClient.getInstance(this.devMode)
                                        .ensembleIsReady(ensembleObj)) {
                            Thread.sleep(waitTime);
                            count++;
                        }
                    }
                }
            }

            if (model.matches(MODEL_RE)) {
                JSONObject modelObj = BigMLClient.getInstance(this.devMode).getModel(
                            model);
                if (modelObj != null) {
                    modelJSON = modelObj;
                    if (waitTime > 0) {
                        int count = 0;
                        while (count < retries
                                && !BigMLClient.getInstance(this.devMode)
                                        .modelIsReady(modelObj)) {
                            Thread.sleep(waitTime);
                            count++;
                        }
                    }
                }
            }

            if (model.matches(LOGISTICREGRESSION_RE)) {
                JSONObject logisticRegressionObj =
                    BigMLClient.getInstance(this.devMode).getLogisticRegression(
                            model);
                if (logisticRegressionObj != null) {
                    modelJSON = logisticRegressionObj;
                    if (waitTime > 0) {
                        int count = 0;
                        while (count < retries
                                && !BigMLClient.getInstance(this.devMode)
                                        .logisticRegressionIsReady(logisticRegressionObj)) {
                            Thread.sleep(waitTime);
                            count++;
                        }
                    }
                }
            }

            // Input data
            JSONObject inputDataJSON = null;
            if (inputData == null) {
                inputDataJSON = new JSONObject();
            } else {
                if (byName && !model.matches(ENSEMBLE_RE)) {
                    JSONObject fields = (JSONObject) Utils.getJSONObject(modelJSON,
                            "object.model.fields");

                    if (fields != null) {
                        JSONObject invertedFields = Utils.invertDictionary(fields);
                        inputDataJSON = new JSONObject();
                        Iterator iter = inputData.keySet().iterator();
                        while (iter.hasNext()) {
                            String key = (String) iter.next();
                            if (invertedFields.get(key) != null) {
                                inputDataJSON.put( ((JSONObject) invertedFields.get(key)).get("fieldID"), inputData.get(key));
                            }
                        }
                    } else {
                        inputDataJSON = new JSONObject();
                    }

                } else {
                    inputDataJSON = inputData;
                }
            }

            JSONObject requestObject = new JSONObject();
            if (args != null) {
                requestObject = args;
            }

            if (model.matches(MODEL_RE)) {
                requestObject.put("model", model);
            }
            if (model.matches(ENSEMBLE_RE)) {
                requestObject.put("ensemble", model);
            }
            if (model.matches(LOGISTICREGRESSION_RE)) {
                requestObject.put("logisticregression", model);
            }

            requestObject.put("input_data", inputDataJSON);

            return createResource(PREDICTION_URL, requestObject.toJSONString());

        } catch (Throwable e) {
            logger.error("Error creating prediction", e);
            return null;
        }

    }

    /**
     * Retrieves a prediction.
     *
     * GET /andromeda/prediction/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     *
     * @param predictionId
     *            a unique identifier in the form prediction/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public JSONObject get(final String predictionId) {
        if (predictionId == null || predictionId.length() == 0
                || !predictionId.matches(PREDICTION_RE)) {
            logger.info("Wrong prediction id");
            return null;
        }

        return getResource(BIGML_URL + predictionId);
    }

    /**
     * Retrieves a prediction.
     *
     * GET /andromeda/prediction/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1 Host: bigml.io
     *
     * @param prediction
     *            an prediction JSONObject
     *
     */
    @Override
    public JSONObject get(final JSONObject prediction) {
        String resourceId = (String) prediction.get("resource");
        return get(resourceId);
    }

    /**
     * Checks whether a prediction's status is FINISHED.
     *
     * @param predictionId
     *            a unique identifier in the form prediction/id where id is a
     *            string of 24 alpha-numeric chars.
     *
     */
    @Override
    public boolean isReady(final String predictionId) {
        return isResourceReady(get(predictionId));
    }

    /**
     * Checks whether a prediction's status is FINISHED.
     *
     * @param prediction
     *            a prediction JSONObject
     *
     */
    @Override
    public boolean isReady(final JSONObject prediction) {
        String resourceId = (String) prediction.get("resource");
        return isReady(resourceId);
    }

    /**
     * Lists all your predictions.
     *
     * GET
     * /andromeda/prediction?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * Host: bigml.io
     *
     * @param queryString
     *            query filtering the listing.
     *
     */
    @Override
    public JSONObject list(final String queryString) {
        return listResources(PREDICTION_URL, queryString);
    }

    /**
     * Updates a prediction.
     *
     * PUT /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param predictionId
     *            a unique identifier in the form prediction/id where id is a
     *            string of 24 alpha-numeric chars.
     * @param changes
     *            set of parameters to update the source. Optional
     *
     */
    @Override
    public JSONObject update(final String predictionId, final String changes) {
        if (predictionId == null || predictionId.length() == 0
                || !predictionId.matches(PREDICTION_RE)) {
            logger.info("Wrong prediction id");
            return null;
        }
        return updateResource(BIGML_URL + predictionId, changes);
    }

    /**
     * Updates a prediction.
     *
     * PUT /andromeda/model/id?username=$BIGML_USERNAME;api_key=$BIGML_API_KEY;
     * HTTP/1.1 Host: bigml.io Content-Type: application/json
     *
     * @param prediction
     *            a prediction JSONObject
     * @param changes
     *            set of parameters to update the source. Optional
     *
     */
    @Override
    public JSONObject update(final JSONObject prediction,
            final JSONObject changes) {
        String resourceId = (String) prediction.get("resource");
        return update(resourceId, changes.toJSONString());
    }

    /**
     * Deletes a prediction.
     *
     * DELETE /andromeda/prediction/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     *
     * @param predictionId
     *            a unique identifier in the form prediction/id where id is a
     *            string of 24 alpha-numeric chars
     *
     */
    @Override
    public JSONObject delete(final String predictionId) {
        if (predictionId == null || predictionId.length() == 0
                || !predictionId.matches(PREDICTION_RE)) {
            logger.info("Wrong prediction id");
            return null;
        }
        return deleteResource(BIGML_URL + predictionId);
    }

    /**
     * Deletes a prediction.
     *
     * DELETE /andromeda/prediction/id?username=$BIGML_USERNAME;api_key=
     * $BIGML_API_KEY; HTTP/1.1
     *
     * @param prediction
     *            a prediction JSONObject
     *
     */
    @Override
    public JSONObject delete(final JSONObject prediction) {
        String resourceId = (String) prediction.get("resource");
        return delete(resourceId);
    }

}
